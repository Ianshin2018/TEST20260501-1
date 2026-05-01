# Library System

這是一個以 Spring Boot 建立的圖書借閱示範專案，提供最基本的會員註冊、登入、書籍查詢、借書與還書流程。  
專案內建簡單前端頁面，也提供 REST API，適合拿來做課堂作業、作品展示、Java Web 練習，或當成後續擴充的基礎。

## 專案功能

- 會員註冊
- 會員登入
- 取得登入 token
- 查詢館藏清單
- 借閱書籍
- 歸還書籍
- 未登入狀態下限制借還書
- 使用 H2 資料庫初始化資料表與測試資料

## 使用技術

- Java 21
- Spring Boot 3.3.5
- Maven
- Spring Web
- Spring JDBC
- H2 Database
- Vue 3

## 第一次使用前請先確認

你需要先安裝：

- JDK 21
- Maven 3.9 以上

可用以下指令確認版本：

```powershell
java -version
mvn -version
```

## 專案結構

```text
src/main/java/com/example/library
├─ controller    API 入口
├─ service       業務邏輯
├─ repository    資料存取
├─ security      簡易登入與 token 驗證
├─ dto           請求與回應資料模型
├─ db            H2 Stored Procedure 實作
└─ config        Web 設定

src/main/resources
├─ application.properties   專案設定
└─ static/index.html        內建前端頁面

DB
├─ ddl.sql   建表腳本
└─ dml.sql   初始資料
```

## API 一覽

- `POST /api/auth/register`
  註冊新會員
- `POST /api/auth/login`
  會員登入並取得 token
- `GET /api/inventories`
  查詢館藏清單
- `POST /api/borrowings/borrow`
  借書，需要登入 token
- `POST /api/borrowings/return`
  還書，需要登入 token

## 如何啟動專案

### 方式 1：推薦，用 jar 啟動

這個專案所在資料夾若包含中文路徑，`spring-boot:run` 有機率出現 classpath 問題。  
最穩定的方式是先打包，再用 `java -jar` 啟動。

```powershell
mvn "-Dmaven.repo.local=./.m2repo" clean package
java -jar target\library-system-0.0.1-SNAPSHOT.jar
```

啟動成功後可開啟：

- 首頁：`http://localhost:8080`
- H2 Console：`http://localhost:8080/h2-console`

### 方式 2：用 IDE 啟動

如果你使用 IntelliJ IDEA 或 Eclipse，也可以直接執行：

- `src/main/java/com/example/library/LibraryApplication.java`

### 方式 3：使用 `spring-boot:run`

如果你的專案路徑是純英文，通常可以直接執行：

```powershell
mvn "-Dmaven.repo.local=./.m2repo" spring-boot:run
```

如果你目前的專案路徑含有中文，建議優先改用「方式 1」。

## 預設設定

目前專案預設使用：

- Port：`8080`
- 資料庫：H2 檔案資料庫
- JDBC URL：`jdbc:h2:file:./DB/librarydb`

對應設定位於 [src/main/resources/application.properties](/d:/test/新增資料夾%20(2)/src/main/resources/application.properties)。

## 如何手動測試

### 1. 用網頁測試

啟動後開啟：

```text
http://localhost:8080
```

你可以依序測試：

1. 註冊新帳號
2. 登入
3. 查看館藏
4. 借書
5. 還書
6. 重複註冊是否被阻擋
7. 未登入是否不能借書

### 2. 用 API 工具測試

你可以用 Postman、curl 或 Bruno 測試。

#### 註冊

```bash
curl -X POST http://localhost:8080/api/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"phoneNumber\":\"0912345678\",\"password\":\"password123\",\"userName\":\"TestUser\"}"
```

#### 登入

```bash
curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"phoneNumber\":\"0912345678\",\"password\":\"password123\"}"
```

登入成功後，請從回傳結果取出 `authToken`。

#### 查詢館藏

```bash
curl http://localhost:8080/api/inventories
```

#### 借書

```bash
curl -X POST http://localhost:8080/api/borrowings/borrow ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer 你的token" ^
  -d "{\"inventoryId\":1}"
```

#### 還書

```bash
curl -X POST http://localhost:8080/api/borrowings/return ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer 你的token" ^
  -d "{\"inventoryId\":1}"
```

## 如何檢查資料庫

啟動專案後打開：

```text
http://localhost:8080/h2-console
```

連線資訊：

- JDBC URL：`jdbc:h2:file:./DB/librarydb`
- Username：`sa`
- Password：留空

## 執行測試

執行單元與整合測試：

```powershell
mvn "-Dmaven.repo.local=./.m2repo" test
```

重新編譯並完整測試：

```powershell
mvn "-Dmaven.repo.local=./.m2repo" clean test
```

目前專案已有測試案例，包含：

- 註冊成功
- 重複註冊失敗
- 登入成功
- 未登入借書失敗
- 借書成功
- 還書成功
- 同一本書不可重複借出

測試檔位於 [AuthControllerTest.java](/d:/test/新增資料夾%20(2)/src/test/java/com/example/library/AuthControllerTest.java)。

## 常見問題

### `spring-boot:run` 無法啟動

如果出現類似以下錯誤：

```text
ClassNotFoundException: com.example.library.LibraryApplication
```

通常是因為專案所在路徑含中文或特殊字元，導致 Maven 啟動子程序時 classpath 處理失敗。  
建議改用：

```powershell
mvn "-Dmaven.repo.local=./.m2repo" clean package
java -jar target\library-system-0.0.1-SNAPSHOT.jar
```

### Port 8080 被佔用

如果 `8080` 已被其他程式使用，可以修改 [application.properties](/d:/test/新增資料夾%20(2)/src/main/resources/application.properties) 中的：

```properties
server.port=8080
```

例如改成：

```properties
server.port=8081
```

## 補充說明

- `DB/ddl.sql` 會建立資料表與 H2 Stored Procedure
- `DB/dml.sql` 會寫入初始資料
- 前端頁面位於 `src/main/resources/static/index.html`
- 這個專案目前採用簡易 token 驗證流程，適合示範與學習用途
