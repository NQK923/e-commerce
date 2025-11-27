rootProject.name = "ecommerce-core"

fun includeModule(path: String) {
    val projectPath = ":$path"
    include(projectPath)
    project(projectPath).projectDir = file("backend/${path.replace(":", "/")}")
}

listOf(
    // common group
    "common:common-domain",
    "common:common-application",
    "common:common-infrastructure",

    // identity
    "identity:identity-domain",
    "identity:identity-application",
    "identity:identity-adapter",
    "identity:identity-infrastructure",

    // product
    "product:product-domain",
    "product:product-application",
    "product:product-adapter",
    "product:product-infrastructure",

    // order
    "order:order-domain",
    "order:order-application",
    "order:order-adapter",
    "order:order-infrastructure",

    // inventory
    "inventory:inventory-domain",
    "inventory:inventory-application",
    "inventory:inventory-adapter",
    "inventory:inventory-infrastructure",

    // promotion
    "promotion:promotion-domain",
    "promotion:promotion-application",
    "promotion:promotion-adapter",
    "promotion:promotion-infrastructure",

    // logistics
    "logistics:logistics-domain",
    "logistics:logistics-application",
    "logistics:logistics-adapter",
    "logistics:logistics-infrastructure",

    // notification
    "notification:notification-domain",
    "notification:notification-application",
    "notification:notification-adapter",
    "notification:notification-infrastructure",

    // report
    "report:report-domain",
    "report:report-application",
    "report:report-adapter",
    "report:report-infrastructure",

    // cart
    "cart:cart-domain",
    "cart:cart-application",
    "cart:cart-adapter",
    "cart:cart-infrastructure",

    // bootstrap
    "bootstrap",
).forEach { includeModule(it) }
