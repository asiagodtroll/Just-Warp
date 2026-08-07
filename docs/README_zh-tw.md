# JustWarp

JustWarp 是透過指令與唯讀箱子選單管理群組傳送點的純伺服器端 Fabric 模組 原版客戶端不需要安裝模組

[English](../README.md) · [設定說明](config.md) · [變更紀錄](../CHANGELOG.md)

## 環境需求

- Minecraft 26.2
- Fabric Loader 0.19.3 或更新版本
- Fabric API 0.156.0+26.2
- Java 25

將 JustWarp 與 Fabric API 放入伺服器的 `mods` 目錄 設定檔會建立在 `config/justwarp/`

## 指令

| 指令 | 功能 | 權限 |
| --- | --- | --- |
| `/jw` | 開啟主選單 | 所有人 |
| `/jw help` | 顯示可用指令 | 所有人 |
| `/jw back` | 返回上一次傳送前的位置 | 所有人 |
| `/jw warp` | 瀏覽傳送點 | 所有人 |
| `/jw warp add <名稱> <圖示> [群組]` | 新增玩家目前位置 | 管理員 |
| `/jw warp del <名稱>` | 刪除傳送點 | 管理員 |
| `/jw warp set <名稱> <欄位> <值>` | 更新傳送點 | 管理員 |
| `/jw warp set <名稱> position` | 更新傳送點位置 | 管理員 |
| `/jw group` | 瀏覽群組 | 所有人 |
| `/jw group add <名稱> <圖示>` | 新增群組 | 管理員 |
| `/jw group del <名稱>` | 刪除群組 | 管理員 |
| `/jw group set <名稱> <欄位> <值>` | 更新群組 | 管理員 |
| `/jw icon` | 瀏覽自訂圖示 | 所有人 |
| `/jw icon add <名稱> <base64>` | 新增自訂圖示 | 管理員 |
| `/jw icon del <名稱>` | 刪除未使用的圖示 | 管理員 |
| `/jw icon set <名稱> <base64>` | 更新自訂圖示 | 管理員 |
| `/jw reload` | 驗證並重新載入 JSON | 管理員 |

預設管理權限等級為 2 名稱區分大小寫、支援 Unicode 且不可包含空白 中文、空白與特殊字元文字值
必須使用雙引號 Tab 補全會自動加入並跳脫引號 例如 `/jw warp del "傳送點"` 或
`/jw warp set "傳送點" description "中文說明"` 圖示可使用自訂名稱或加引號的 namespaced 物品 ID
例如 `"minecraft:stone"` 使用 `none` 可移除傳送點群組

## 主要行為

- GUI 保留群組、傳送點與圖示的 JSON 順序
- 選單支援分頁且不能移動物品
- 可設定傳送安全模式
- 無效的 reload 不會取代目前資料或覆寫來源檔案
- 指令變更會立即儲存

設定欄位與資料檔案請參閱 [`config.md`](config.md)

## 授權

本專案採用 MIT License
