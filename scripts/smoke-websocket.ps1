param(
    [string]$ApiBaseUrl = "http://localhost:8080",
    [string]$BuyerEmail = "buyer@example.local",
    [string]$BuyerPassword = "Buyer@123",
    [string]$BuyerUserId = "00000000-0000-0000-0000-000000000101",
    [string]$SellerEmail = "seller@example.local",
    [string]$SellerPassword = "Seller@123",
    [string]$SellerUserId = "00000000-0000-0000-0000-000000000102",
    [int]$TimeoutSeconds = 15
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

function Join-Url {
    param(
        [string]$Base,
        [string]$Path
    )

    return $Base.TrimEnd("/") + "/" + $Path.TrimStart("/")
}

function ConvertTo-WebSocketUrl {
    param([string]$HttpUrl)

    $base = [Uri]$HttpUrl
    $scheme = if ($base.Scheme -eq "https") { "wss" } else { "ws" }
    $authority = $base.Authority
    return "${scheme}://${authority}/ws/chat"
}

function Invoke-Login {
    param(
        [string]$Email,
        [string]$Password
    )

    $body = @{ email = $Email; password = $Password } | ConvertTo-Json -Compress
    $response = Invoke-RestMethod `
        -Uri (Join-Url $ApiBaseUrl "/api/auth/login") `
        -Method POST `
        -ContentType "application/json" `
        -Body $body

    if (-not $response.accessToken) {
        throw "Login response for $Email did not include accessToken."
    }
    return $response.accessToken
}

function New-StompFrame {
    param(
        [string]$Command,
        [hashtable]$Headers = @{},
        [string]$Body = ""
    )

    $builder = [System.Text.StringBuilder]::new()
    [void]$builder.Append($Command).Append("`n")
    foreach ($key in $Headers.Keys) {
        [void]$builder.Append($key).Append(":").Append($Headers[$key]).Append("`n")
    }
    [void]$builder.Append("`n")
    [void]$builder.Append($Body)
    [void]$builder.Append([char]0)
    return $builder.ToString()
}

function Send-StompFrame {
    param(
        [System.Net.WebSockets.ClientWebSocket]$Socket,
        [string]$Command,
        [hashtable]$Headers = @{},
        [string]$Body = ""
    )

    $frame = New-StompFrame -Command $Command -Headers $Headers -Body $Body
    $bytes = [System.Text.Encoding]::UTF8.GetBytes($frame)
    $segment = [ArraySegment[byte]]::new($bytes)
    $Socket.SendAsync($segment, [System.Net.WebSockets.WebSocketMessageType]::Text, $true, [System.Threading.CancellationToken]::None).GetAwaiter().GetResult()
}

function Receive-StompFrame {
    param(
        [System.Net.WebSockets.ClientWebSocket]$Socket,
        [string]$Label,
        [int]$TimeoutSeconds
    )

    $buffer = New-Object byte[] 8192
    $builder = [System.Text.StringBuilder]::new()
    $deadline = [DateTimeOffset]::UtcNow.AddSeconds($TimeoutSeconds)

    while ([DateTimeOffset]::UtcNow -lt $deadline) {
        $remaining = [Math]::Max(1, [int]($deadline - [DateTimeOffset]::UtcNow).TotalMilliseconds)
        $cts = [System.Threading.CancellationTokenSource]::new($remaining)
        try {
            $result = $Socket.ReceiveAsync([ArraySegment[byte]]::new($buffer), $cts.Token).GetAwaiter().GetResult()
        } catch [OperationCanceledException] {
            break
        } finally {
            $cts.Dispose()
        }

        if ($result.MessageType -eq [System.Net.WebSockets.WebSocketMessageType]::Close) {
            throw "WebSocket closed while waiting for $Label."
        }

        [void]$builder.Append([System.Text.Encoding]::UTF8.GetString($buffer, 0, $result.Count))
        $current = $builder.ToString()
        if ($current.Contains([char]0)) {
            $frame = $current.Substring(0, $current.IndexOf([char]0))
            while ($frame.StartsWith("`n") -or $frame.StartsWith("`r")) {
                $frame = $frame.Substring(1)
            }
            return $frame
        }
    }

    throw "Timed out waiting for STOMP frame: $Label."
}

function Assert-StompCommand {
    param(
        [string]$Frame,
        [string]$ExpectedCommand,
        [string]$Label
    )

    if (-not ($Frame.StartsWith("$ExpectedCommand`n") -or $Frame.StartsWith("$ExpectedCommand`r`n"))) {
        throw "Expected $Label to be $ExpectedCommand but received: $Frame"
    }
}

function Get-StompBody {
    param([string]$Frame)

    $separator = "`n`n"
    $index = $Frame.IndexOf($separator)
    if ($index -lt 0) {
        $separator = "`r`n`r`n"
        $index = $Frame.IndexOf($separator)
    }
    if ($index -lt 0) {
        return ""
    }
    return $Frame.Substring($index + $separator.Length)
}

function Connect-Stomp {
    param(
        [string]$Label,
        [string]$AccessToken
    )

    $socket = [System.Net.WebSockets.ClientWebSocket]::new()
    $socket.ConnectAsync([Uri](ConvertTo-WebSocketUrl $ApiBaseUrl), [System.Threading.CancellationToken]::None).GetAwaiter().GetResult()
    Send-StompFrame `
        -Socket $socket `
        -Command "CONNECT" `
        -Headers @{
            "accept-version" = "1.2"
            "heart-beat" = "0,0"
            "host" = "localhost"
            "Authorization" = "Bearer $AccessToken"
        }
    $connected = Receive-StompFrame -Socket $socket -Label "$Label CONNECTED" -TimeoutSeconds $TimeoutSeconds
    Assert-StompCommand -Frame $connected -ExpectedCommand "CONNECTED" -Label "$Label connect response"
    return $socket
}

function Subscribe-Stomp {
    param(
        [System.Net.WebSockets.ClientWebSocket]$Socket,
        [string]$SubscriptionId,
        [string]$Destination
    )

    Send-StompFrame `
        -Socket $Socket `
        -Command "SUBSCRIBE" `
        -Headers @{
            "id" = $SubscriptionId
            "destination" = $Destination
            "ack" = "auto"
        }
}

$buyerSocket = $null
$sellerSocket = $null

try {
    Write-Host "Logging in seeded smoke users..."
    $buyerToken = Invoke-Login -Email $BuyerEmail -Password $BuyerPassword
    $sellerToken = Invoke-Login -Email $SellerEmail -Password $SellerPassword

    Write-Host "Connecting STOMP clients to $(ConvertTo-WebSocketUrl $ApiBaseUrl)..."
    $buyerSocket = Connect-Stomp -Label "buyer" -AccessToken $buyerToken
    $sellerSocket = Connect-Stomp -Label "seller" -AccessToken $sellerToken

    Subscribe-Stomp -Socket $buyerSocket -SubscriptionId "buyer-chat" -Destination "/user/queue/chat/messages"
    Subscribe-Stomp -Socket $buyerSocket -SubscriptionId "buyer-notifications" -Destination "/user/queue/notifications"
    Subscribe-Stomp -Socket $sellerSocket -SubscriptionId "seller-ack" -Destination "/user/queue/chat/ack"

    $content = "websocket-smoke-$([DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds())"
    $messageBody = @{
        conversationId = ""
        receiverId = $BuyerUserId
        content = $content
    } | ConvertTo-Json -Compress

    Write-Host "Sending chat message from seller $SellerUserId to buyer $BuyerUserId..."
    Send-StompFrame `
        -Socket $sellerSocket `
        -Command "SEND" `
        -Headers @{
            "destination" = "/app/chat.send"
            "content-type" = "application/json"
        } `
        -Body $messageBody

    $sellerAck = Receive-StompFrame -Socket $sellerSocket -Label "seller chat ack" -TimeoutSeconds $TimeoutSeconds
    Assert-StompCommand -Frame $sellerAck -ExpectedCommand "MESSAGE" -Label "seller chat ack"
    $ackBody = Get-StompBody $sellerAck | ConvertFrom-Json
    if ($ackBody.persistedMessage.content -ne $content) {
        throw "Seller ack did not contain the expected message content."
    }

    $buyerDelivery = Receive-StompFrame -Socket $buyerSocket -Label "buyer chat delivery" -TimeoutSeconds $TimeoutSeconds
    Assert-StompCommand -Frame $buyerDelivery -ExpectedCommand "MESSAGE" -Label "buyer chat delivery"
    $deliveryBody = Get-StompBody $buyerDelivery | ConvertFrom-Json
    if ($deliveryBody.content -ne $content) {
        throw "Buyer delivery did not contain the expected message content."
    }
    if ($deliveryBody.senderId -ne $SellerUserId -or $deliveryBody.receiverId -ne $BuyerUserId) {
        throw "Buyer delivery sender/receiver did not match the seeded smoke users."
    }

    Send-StompFrame `
        -Socket $buyerSocket `
        -Command "SEND" `
        -Headers @{
            "destination" = "/app/notification.read"
            "content-type" = "application/json"
        } `
        -Body (@{ notificationId = "websocket-smoke-read-ack" } | ConvertTo-Json -Compress)

    Write-Host "WebSocket smoke passed: authenticated STOMP connect, user subscriptions, chat ack, and chat delivery."
} finally {
    foreach ($socket in @($buyerSocket, $sellerSocket)) {
        if ($socket -and $socket.State -eq [System.Net.WebSockets.WebSocketState]::Open) {
            Send-StompFrame -Socket $socket -Command "DISCONNECT"
            $socket.Dispose()
        } elseif ($socket) {
            $socket.Dispose()
        }
    }
}
