# Java 實作題

## 技術要求

- 使用 `Vue.js` 作為前端技術。
- 使用 `Spring Boot` 搭建相關應用程式。
- 使用 `RESTful API` 風格建立後端服務。
- 使用 `Maven` 或 `Gradle` 作為專案建置工具。
- 透過 `Stored Procedure` 存取資料庫。
- 需同時異動多個資料表時，請實作 `Transaction`，避免資料錯亂。
- 資料庫的 `DDL` 和 `DML` 請存放在專案下的 `DB` 資料夾內提供。
- 需防止 `SQL Injection` 以及 `XSS` 攻擊。

## 系統架構要求

- 使用 `Web Server` + `Application Server` + 任一關聯式資料庫的三層式架構。
- 後端依照需求設計展示層、業務層、資料層以及共用層。

## 資料表需求

### User 使用者

| 欄位名稱 | 說明 |
| --- | --- |
| `User Id` (PK) | 使用者 ID，唯一值，用來做資料庫關聯。 |
| `Phone Number` | 手機號碼，註冊時須檢查不可重複，並作為登入帳號識別用。 |
| `Password` | 密碼需加鹽（salt）並經雜湊（Hash）後儲存，避免明碼外洩。 |
| `User Name` | 使用者名稱。 |
| `Registration Time` | 註冊日期時間。 |
| `Last Login Time` | 最後登入時間。 |
| 其他欄位 | 可依需求自行補充。 |

### Inventory 庫存

| 欄位名稱 | 說明 |
| --- | --- |
| `Inventory Id` (PK) | 庫存 ID，唯一值。 |
| `ISBN` | 國際標準書號。 |
| `Store Time` | 書籍入庫（購買）日期時間。 |
| `Status` | 書籍狀態，例如：在庫、出借中、整理中（歸還後未入庫）、遺失、損毀、廢棄。 |
| 其他欄位 | 可依需求自行補充。 |

### Book 書籍

| 欄位名稱 | 說明 |
| --- | --- |
| `ISBN` (PK) | 國際標準書號。 |
| `Name` | 書名。 |
| `Author` | 作者。 |
| `Introduction` | 書籍內容簡介。 |
| 其他欄位 | 可依需求自行補充。 |

### Borrowing Record 借閱紀錄

| 欄位名稱 | 說明 |
| --- | --- |
| `User Id` (index 1) | 使用者 ID。 |
| `Inventory Id` (index 2) | 庫存 ID。 |
| `Borrowing Time` | 借出日期時間。 |
| `Return Time` | 歸還日期時間。 |
| 其他欄位 | 可依需求自行補充。 |
