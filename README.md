# Nền tảng E-commerce - Dự án Thương Mại Điện Tử

## 1. Mục tiêu & Phạm vi
- Xây dựng nền tảng thương mại điện tử cấp doanh nghiệp, ưu tiên tính toàn vẹn dữ liệu, bảo mật và khả năng mở rộng.
- **Kiến trúc**: Modular Monolith + Clean Architecture (DDD) giúp tách biệt rõ ràng giữa domain, ứng dụng, adapter và infrastructure; dễ dàng mở rộng và chuyển đổi thành microservices khi cần.
- **Trải nghiệm**:
  - Khách mua hàng: Duyệt sản phẩm, giỏ hàng, thanh toán, chat với người bán, theo dõi đơn hàng.
  - Người bán: Đăng ký, quản lý sản phẩm (thêm/sửa/xóa, bulk import), khuyến mãi (flash sale, coupon), quản lý vận hành bán hàng.

## 2. Kiến trúc Tổng quan
Dự án được xây dựng dựa trên **Multi-module Gradle Kotlin DSL** (Java 17, Spring Boot 3.5.8). Mỗi "bounded context" được chia thành các module tương ứng:
- `*-domain`: Lõi nghiệp vụ (không phụ thuộc Spring/JPA/Kafka/HTTP).
- `*-application`: Chứa use cases, port, DTO.
- `*-adapter`: Chứa REST controllers, Kafka consumers, Websocket, Spring Security.
- `*-infrastructure`: JPA, Redis, Kafka, Elasticsearch, Flyway.
- `bootstrap`: Module duy nhất khởi động toàn bộ ứng dụng Spring Boot.

**Luồng phụ thuộc**: `infrastructure / adapter -> application -> domain -> common-domain`.

### Các Bounded Context chính:
| Context | Chức năng nổi bật | Adapter/Infra tiêu biểu |
| --- | --- | --- |
| **common** | Value Object, Identifier, pagination, utils... | Chứa các JPA base, bảo vệ tầng domain/app khỏi phụ thuộc framework. |
| **identity** | Đăng ký/Đăng nhập, OTP, Refresh token, Profile & Địa chỉ, Duyệt seller. | REST API `/api/auth`, `/api/users`, OAuth2 Google/Facebook, Redis/Mail (OTP). |
| **product** | CRUD sản phẩm, SKU, Hình ảnh, Review/Report, Bulk import, Search. | REST API `/api/products`, Elasticsearch, Redis cache. |
| **promotion** | Flash Sale, Coupon, Đồng bộ tồn kho khi khuyến mãi. | REST API `/api/flash-sales`, `/api/admin/flash-sales`, `/api/coupons`, Redis (cho tồn kho flash sale). |
| **cart** | Giỏ hàng cho cả người dùng ẩn danh (Guest) & đã đăng nhập. | REST API `/api/cart`, Đồng bộ localStorage & API. |
| **order** | Quy trình đặt hàng, Thanh toán VNPay, Outbox pattern xử lý sự kiện. | REST API `/api/orders`, `/api/orders/{id}/payment/vnpay`, Outbox & Kafka events. |
| **inventory** | Quản lý tồn kho, Hold/Reserve hàng cho Order, Flash sale. | REST API `/api/inventory`, Redis & Spring Data JPA. |
| **logistics** | Tiện ích liên quan tới phí vận chuyển, tracking. | REST API `/api/logistics`. |
| **notification** | Lưu trữ và đẩy thông báo theo thời gian thực. | REST API `/api/notifications`, WebSocket (`/user/queue/notifications`). |
| **report** | Chức năng báo cáo bán hàng trong ngày. | REST API `/api/reports/daily?date=yyyy-MM-dd`. |
| **chat** | Chat realtime giữa Buyer & Seller, Read receipt. | REST `/api/chat/**`, STOMP qua Websocket `/ws/chat`, MongoDB lưu trữ tin nhắn. |

## 3. Công nghệ Tiêu biểu
### Backend
- **Core**: Java 17, Spring Boot 3.5.8, Spring Security 6, Spring Data JPA, Lombok.
- **Database**: PostgreSQL (Migrations qua Flyway: `V1...V9`).
- **NoSQL & Broker**: 
  - Redis: Dùng cho session, rate limit, OTP, caching, xử lý stock chớp nhoáng (flash sale).
  - MongoDB: Lưu trữ dữ liệu chat.
  - Elasticsearch: Chỉ mục tìm kiếm sản phẩm.
  - Kafka: Core message broker trong `order` context (Outbox pattern).
- **Giao tiếp**: WebSocket trực tiếp với STOMP.
- **Tích hợp**: VNPay (Thanh toán), OAuth2 Resource Server JWT, Java Mail SMTP.
- **Cấu hình**: `application.yml` + `.env`.

### Frontend
- **Công nghệ**: Next.js 16 (App Router), React 19, TypeScript, Tailwind CSS v4.
- **Quản lý state**: Zustand context-based stores (`auth-store`, `cart-store`, `chat-store`...).
- **Realtime**: SockJS, `@stomp/stompjs` (chat & notification realtime).
- **Lưu trữ tĩnh**: Supabase Storage cho bucket ảnh sản phẩm (`product-images`), giấy tờ seller (`seller-assets`), thư mục chat (`chat-attachments`).
- **Ngôn ngữ (i18n)**: Hỗ trợ tiếng Anh (`en`) và Tiếng Việt (`vi`).

## 4. Setup & Chạy dự án (Development Mode)

### Môi trường yều cầu (Dependencies):
Để ứng dụng hoạt động đầy đủ, bạn cần cài đặt / chạy thông qua các dịch vụ Database và Message Broker:
- PostgreSQL
- Kafka & Zookeeper
- Redis
- Elasticsearch
- MongoDB

Dự án có sẵn `docker-compose.yml` định nghĩa sẵn Kafka và Redis. Các dịch vụ khác tùy chỉnh qua `.env` của backend.

### Chạy Backend
Tạo file `.env` ở root dựa theo template của dự án, rồi dùng Gradle để chạy/dipli:
```bash
# Compile và chạy toàn bộ tests
./gradlew build

# Chạy server ứng dụng
./gradlew :bootstrap:bootRun

# Test chỉ định 1 module cụ thể (Ví dụ module product)
./gradlew :product:product-application:test
```

### Chạy Frontend
Di chuyển vào thư mục `e-commerce-frontend` và làm theo các bước:
```bash
cd e-commerce-frontend

# Cài đặt thư viện
npm install

# Start development server ở http://localhost:3000
npm run dev

# Build để deploy
npm run build
npm run start
```

> **Lưu ý**: Cần thêm đúng URL của Supabase & public token vào file `.env` của Frontend (vd: `NEXT_PUBLIC_SUPABASE_URL`, `NEXT_PUBLIC_SUPABASE_ANON_KEY`).

## 5. Cấu hình Môi trường (.env & settings)
- **Backend**: Cần có các tham số như DB truy cập (`DB_URL`, `DB_USERNAME`), cấu hình kết nối Redis/Kafka/Mongo, Secret keys OAuth2, Google/Facebook/VNPay API keys.
- **Frontend**: Khai báo base API URL (`NEXT_PUBLIC_API_BASE_URL`), config Supabase Buckets ảnh tải lên (vd: `NEXT_PUBLIC_SUPABASE_PRODUCT_BUCKET`).

## 6. Quy trình Đóng góp & Phát triển Code
- **Cơ sở Dữ Liệu**: Mọi thay đổi về cấu trúc schema Postgres phải được tạo qua script Migration Flyway tại `bootstrap/src/main/resources/db/migration/`. KHÔNG dùng chế độ tự sinh (`ddl-auto=update`)...
- **API Mới**: Add REST API tại thư mục `*-adapter`, nghiệp vụ logic tạo UseCase tại `*-application`. KHÔNG mang các đối tượng Spring/HTTP Request đi sâu vào Domain.
- **Testing**: Bảo đảm các unit tests không bị lỗi khi chạy lệnh `./gradlew test` trước khi commit mã nguồn lên Repo nhánh chính.
- **Giao diện FE**: Sử dụng class API instance (`api-client.ts`) mặc định đã tích hợp Bearer token và chức năng tự refresh token để gọi backend thay vì xài fetch Axios/trực tiếp.

## 7. Bề mặt API (Tóm tắt Chức Năng Chính)
- **Xác Thực / Auth**: `/api/auth/register`, `/api/auth/login`, `/api/auth/otp/request`, `/api/auth/me`
- **Sản Phẩm (`Product`)**: `/api/products` (danh sách / search), `/api/bulk/products`, `/api/product-reviews`
- **Khuyến Mãi (`Promotion`)**: `/api/flash-sales`, `/api/admin/flash-sales`, `/api/coupons`
- **Giỏ Hàng (`Cart`)**: `/api/cart`
- **Đơn Hàng & Thanh Toán (`Order/Payment`)**: `/api/orders`, `/api/orders/{id}/payment/vnpay`, `/api/payments/vnpay/return`
- **Kho Hàng (`Inventory`)**: `/api/inventory/reserve` và `/api/inventory/release`
- **Thông báo (`Notification`)**: `/api/notifications/` và theo dõi qua Websocket `/user/queue/notifications`
- **Trò Chuyện (`Chat`)**: Endpoint `/api/chat/**`, Websocket qua `/ws/chat`, topic gửi nhận `/user/queue/chat/messages`
