# Thermal Foundation — NeoForge 1.21.1 向け修正仕様書

本書は **Arclight** 上で **NeoForge 対応 Thermal Foundation** を動作させるための修正仕様である。  
実装は **Free 枠のみ**で進め、有償クレジット依存の手段は用いない。

---

## 1. 目的

- **Arclight**（`arclight-neoforge-1.21.1-1.0.2-SNAPSHOT-0769551`）環境で **NeoForge 対応 Thermal Foundation** が安定して動作すること。
- **Forge 1.20.1 → NeoForge 1.21.1** の API 変更（特に **ItemStack の Data Components 化**）に追随すること。
- 依存ライブラリ **[CoFHCoreForNeoForge](https://github.com/CoFH/CoFHCore)** の移行方針に整合すること。

---

## 2. 前提（ターゲット環境）

| 項目 | 値 |
|------|-----|
| Minecraft | **1.21.1** |
| サーバ | **Arclight 1.0.2-SNAPSHOT-0769551** |
| Mod ローダ | **NeoForge 21.1.219** |
| 依存 | **cofh_core** 11.0.2+、**thermal**（Thermal Core）11.0.7+ |
| 本リポジトリ | `gradle.properties` 上 **1.21.1 / NeoForge 21.1.219** |

---

## 3. 参照資料

| 資料 | URL |
|------|-----|
| NeoForge 公式 | https://docs.neoforged.net/ |
| Data Components | https://docs.neoforged.net/docs/1.21.1/items/datacomponents |
| Forge Wiki | https://forge.gemwire.uk/wiki/Main_Page |
| Forge / NeoForge 比較表 | https://docs.google.com/spreadsheets/d/1_DQELiPvCF0FmFfyU4opGDbWi7zv-bSl8ImZuh6645E/edit?gid=248444698 |
| 修正参考（CoFH Core） | `C:\Users\SPLIGAN\Documents\GitHub\CoFHCoreForNeoForge\docs\NEOFORGE_1.21.1_MIGRATION_SPEC.md` |

---

## 4. スコープ

### 4.1 本リポジトリ（Thermal Foundation）

- ブロック・アイテム・エンティティ登録（Thermal Core の `BLOCKS` / `ITEMS` / `ENTITIES` 経由）
- ワールド生成（Configured / Placed Feature、Biome Modifier）
- データ生成（ルート、レシピ、タグ、モデル）
- イベント（Wanderer 取引など）
- `META-INF/neoforge.mods.toml`

### 4.2 依存側（別リポジトリ・別タスク）

| 依存 | リポジトリ | 状態 |
|------|-----------|------|
| cofh_core | CoFHCoreForNeoForge | **コンパイル成功**（仕様書 §14 参照） |
| thermal | ThermalCoreForNeoForge | **compileJava 成功**（2026-05-17）— 残りは JEI 非推奨 API 等の警告のみ |
| thermal_foundation | 本リポジトリ | **compileJava 成功**（2026-05-17） |

**Foundation 単体の `compileJava` は Thermal Core のビルド成功が前提**（`settings.gradle` の `includeBuild('../ThermalCoreForNeoForge')`）。**2026-05-17 時点で両方成功。**

---

## 5. 全体方針

1. **ItemStack**: 本 mod の Java ソースに **廃止 NBT API**（`getTag` / `setTag` / `hasTag` 等）を置かない。永続データは CoFH Core の `CoFHItemData` / バニラ `DataComponents` に委譲。
2. **イベント**: `@Mod.EventBusSubscriber` は NeoForge 21.1 で使用不可。**`ThermalFoundation` コンストラクタで `modEventBus.register` / `NeoForge.EVENT_BUS.register`** に明示登録（CoFH Core と同様）。
3. **ResourceLocation**: `ResourceLocation.fromNamespaceAndPath` / `parse` を使用（既に worldgen 等で適用済み）。
4. **メタデータ**: `META-INF/neoforge.mods.toml` を使用（`build.gradle` の `ProcessResources` と一致）。

---

## 6. 修正仕様 — ビルド・メタデータ

| ID | 内容 | 受入条件 | 状態 |
|----|------|----------|------|
| B-01 | `gradle.properties` を MC **1.21.1** / NeoForge **21.1.219** に設定 | 依存解決成功 | **完了** |
| B-02 | NeoGradle userdev **7.1.25** | `./gradlew` 設定成功 | **完了** |
| B-03 | Java **21** ツールチェーン | `java { toolchain }` 一致 | **完了** |
| B-04 | `META-INF/neoforge.mods.toml` 作成（誤って cofh_core 内容になっていた `mods.toml` を置換） | 起動時依存解決 | **完了** |
| B-05 | `accesstransformer.cfg` 維持 | AT 読込 | **完了** |
| B-06 | Modrinth Maven リポジトリ追加（composite の Thermal Core が `curios` を参照するため `build` / `test` 解決に必要） | `./gradlew build` 成功 | **完了** |
| B-07 | 成果物生成 | `./gradlew jar` 成功 | **完了** |

---

## 7. 修正仕様 — イベント登録（N-08）

| ID | 対象 | 仕様 | 状態 |
|----|------|------|------|
| N-08a | `TFndCommonSetupEvents` | `@Mod.EventBusSubscriber` 削除。`NeoForge.EVENT_BUS.register(TFndCommonSetupEvents.class)` | **完了** |
| N-08b | `TFndDataGen` | `@Mod.EventBusSubscriber` 削除。`modEventBus.register(TFndDataGen.class)` | **完了** |
| N-08c | `ThermalFoundation` | 上記登録をコンストラクタに集約 | **完了** |

---

## 8. 修正仕様 — ソース調査（本リポジトリ Java 19 ファイル）

| ID | 区分 | 結果 |
|----|------|------|
| S-01 | ItemStack 廃止 NBT API | **該当なし**（grep ゼロ） |
| S-02 | `ResourceLocation(String, String)` コンストラクタ | **該当なし**（`fromNamespaceAndPath` 使用済み） |
| S-03 | `@Mod.EventBusSubscriber` | **2 ファイル** — §7 で対応 |
| S-04 | ワールド生成 / Biome Modifier | **1.21.1 API 使用済み**（`NeoForgeRegistries.Keys.BIOME_MODIFIERS` 等） |
| S-05 | Loot（`TFndBlockLootTables`） | CoFH `BlockLootSubProviderCoFH` 委譲。**BE タグ付きドロップなし** |

---

## 9. 修正仕様 — 依存 Thermal Core（ブロッカー）

Thermal Core がコンパイルできるまで Foundation の jar は生成できない。主なエラー分類（2026-05-17）:

| 分類 | 例 | 置換方針（CoFH Core 仕様書準拠） |
|------|-----|--------------------------------|
| イベント | `@Mod.EventBusSubscriber`、`SpawnPlacementRegisterEvent` | 明示登録 / 新イベントクラス |
| バニラ API | `PotionUtils`、`BlockPathTypes`、`MobType` | 1.21.1 パッケージ・Data Components |
| NeoForge 撤廃 | `IPlantable`、`EntityItemPickupEvent` | `TriState` / 新イベント |
| ItemStack NBT | `getOrCreateTag`、`addTagElement` | `CoFHItemData` / `DataComponents` |
| FluidStack | `loadFluidStackFromNBT`、`FriendlyByteBuf.readFluidStack` | codec / `FluidStack.STREAM_CODEC` |

**次の実装優先（2026-05-17 継続）**: デバイスレシピ `Serializer` の `MapCodec` / `streamCodec` 統一、残り device レシピ（HiveExtractor 等）、`Ingredient` / `ItemStack` ネットワーク API。

### 9.1 Thermal Core 修正進捗

| 区分 | 内容 | 状態 |
|------|------|------|
| TC-E01 | `@Mod.EventBusSubscriber` 廃止 → `ThermalCore` で明示登録 | **完了** |
| TC-E02 | `SpawnPlacementRegisterEvent` → `RegisterSpawnPlacementsEvent` / `SpawnPlacementTypes` | **完了** |
| TC-E03 | `EntityItemPickupEvent` → `ItemEntityPickupEvent.Pre` + `TriState` | **完了** |
| TC-N01 | `AugmentableBlockEntity` — `loadAdditional` / `FluidStack.parseOptional` / エンチャント `RegistryAccess` | **完了** |
| TC-N02 | モンスター `defineSynchedData(Builder)`、Basalz/Blitz/Blizz | **完了** |
| TC-N03 | `ResourceLocation` — 約 65 ファイルを `parse` / `fromNamespaceAndPath` に一括置換 | **完了** |
| TC-N04 | `StairBlock`、`onDataPacket(..., Provider)`、`FluidStack.copyWithAmount` | **完了** |
| TC-N05 | ルートテーブル `reloadableRegistries().getLootTable(ResourceKey)`、`FisherBoost` | **完了** |
| TC-N06 | エンティティ `ElementalProjectile` / `ThrownFlorb` / レンダラー `renderToBuffer` 色引数 | **完了** |
| TC-N07 | ブロック `useItemOn` / `ItemInteractionResult`（`HardenedGlassBlock` 等） | **一部完了** |
| TC-N08 | レシピ `RecipeSerializer` — `MapCodec` + `streamCodec`（Machine/Dynamo/Catalyst、FisherBoost） | **完了** |
| TC-N09 | デバイスレシピ Serializer（HiveExtractor、TreeExtractor、RockGen、PotionDiffuser 等） | **完了** |
| TC-N10 | 流体 `BucketItem(stillFluid.get())`、アイテム Data Components（Redprint、Detonator 等） | **完了** |
| TC-N11 | アーマー `ArmorMaterialCoFH.create` / `SoundEvent.value()` / `ItemAttributeModifiers` | **完了** |
| TC-N12 | `FoodProperties` レコード API（GourmandFuelManager） | **完了** |
| TC-N13 | `Registries.ENCHANTMENT` + `CoFHRegistryLookup`（DisenchantmentFuelManager） | **完了** |
| TC-N14 | `PotionBrewing.potionMixes` AT + `BrewerRecipeManager` | **完了** |
| TC-N15 | `ConfigPlacementFilter` → `MapCodec` / `TCorePlacementModifiers` | **完了** |
| TC-N16 | `FurnaceRecipeManager` UTF-8 BOM 除去 | **完了** |
| TC-N17 | Patchouli / JEI 非推奨 API | **警告のみ**（コンパイル可） |

---

## 9.2 Thermal Foundation 修正進捗（2026-05-17）

| ID | 内容 | 状態 |
|----|------|------|
| TF-F01 | `BootstrapContext` タイポ修正（`BootstapContext` → `BootstrapContext`）— worldgen | **完了** |
| TF-F02 | `TFndLootTableProvider` — `HolderLookup.Provider` を `LootTableProviderCoFH` に渡す | **完了** |
| TF-F03 | `TFndBlockLootTables` — コンストラクタ追加、`Enchantments.FORTUNE` を `Holder<Enchantment>` 経由 | **完了** |
| TF-F04 | B-04 / N-08 / S-01〜S-05（本リポジトリ Java） | **完了**（§6〜§8） |
| TF-F05 | `TFndBlockLootTables` — datagen 時は `BlockLootSubProviderCoFH#enchantment`（`registries` 経由）を使用。`CoFHRegistryLookup` 直参照を廃止 | **完了** |
| TF-F06 | `runData` 成功（ルート・レシピ・worldgen・モデル生成） | **完了**（2026-05-17 再検証） |

### 9.3 依存 CoFH Core（ランタイム / datagen ブロッカー）

| ID | 内容 | 状態 |
|----|------|------|
| CC-R01 | `ArmorMaterialCoFH.create(int[])` — 1.21 の `ArmorItem.Type#BODY` 追加に対応（不足要素は 0） | **完了**（CoFHCoreForNeoForge、`publishToMavenLocal` 要） |
| CC-R02 | `BlockLootSubProviderCoFH` — エンチャント Holder を datagen の `registries` から解決（`CoFHRegistryLookup` 廃止） | **完了** |

### 9.4 Thermal Core（ランタイム補足）

| ID | 内容 | 状態 |
|----|------|------|
| TC-R01 | `TCoreItems` アーマー防御配列を 5 要素化（`BODY` スロット = 0） | **完了** |

**開発時**: `runData` / 実機起動前に `CoFHCoreForNeoForge` で `publishToMavenLocal` を実行すること（Maven の cofh_core 11.0.2.0 単体では CC-R01/R02 未反映）。

---

## 10. Arclight 向け検証（結合）

| ID | 検証項目 | 状態 |
|----|----------|------|
| A-01 | 導入・起動 | **未実施**（`compileJava` は成功。Arclight 実機検証待ち） |
| A-02 | 鉱石・ゴムの木のワールド生成 | **未実施** |
| A-03 | ボート・看板・木材ブロック | **未実施** |

---

## 11. 完了定義

1. **Thermal Core** 含めターゲット環境で **`compileJava` / `build` 成功**（B-06 で Modrinth 解決。`jar` は B-07）。
2. 本リポジトリに **ItemStack 廃止 NBT API ゼロ**、**`@Mod.EventBusSubscriber` ゼロ**。
3. Arclight 上で **A-01〜A-03** を最低限実施。

### 11.1 Arclight 実機検証手順（A-01〜A-03）

1. `ThermalCoreForNeoForge` / `ThermalFoundationForNeoForge` で `publishToMavenLocal` または `jar` 出力を `mods/` に配置。
2. **cofh_core**（Maven または CoFHCoreForNeoForge ビルド）、**thermal**（Core jar）、**thermal_foundation**（本 jar）を同一 `mods` に置く。
3. Arclight NeoForge 1.21.1 サーバを起動し、ログに mod ロードエラーがないことを確認（A-01）。
4. 新規ワールドで鉱石（tin/lead 等）・ゴムの木（rubberwood）が生成されることを確認（A-02）。
5. ゴムの木のボート・看板・木材ブロックの設置・クラフトを確認（A-03）。

---

---

## 12. 改訂履歴

| 日付 | 内容 |
|------|------|
| 2026-05-17 | 初版 — リポジトリ調査、Thermal Core ブロッカー特定、B-04 / N-08 実装開始 |
| 2026-05-17 | **B-04 / N-08 完了** — `neoforge.mods.toml` 追加、`ThermalFoundation` で明示イベント登録 |
| 2026-05-17 | **Thermal Core 移植開始** — イベント登録、Data Components、流体 codec、BE NBT API、ピックアップイベント等を CoFH Core 準拠で修正。Foundation `compileJava` は Core 完了待ち |
| 2026-05-17 | **Thermal Core 大量修正** — `ResourceLocation` 一括置換、BE/エンティティ/流体/ルートテーブル、レシピ `MapCodec` 着手。`compileJava` は依然エラーあり（主に device レシピ Serializer） |
| 2026-05-17 | **レシピ Serializer・Item API** — 全 device/Machine 系 Serializer を NeoForge 1.21.1 化。CoFHItemData 移行（Redprint/Detonator）、流体バケツ、アーマー属性 API 着手。`compileJava` エラーは **~100 → 数十件**（Patchouli・一部 BE 等が残存） |
| 2026-05-17 | **レシピ Serializer 完了** — Machine/Dynamo/Catalyst + 全 device レシピを `MapCodec` / `CONTENTS_STREAM_CODEC` 化。流体バケツ・Redprint/Detonator CoFHItemData、Diving/Hazmat アーマー API 着手。`compileJava` は **約数十件**に減少（Patchouli・Wrench・一部 BE 等が残存） |
| 2026-05-17 | **Thermal Core `compileJava` 成功** — BOM 除去、ArmorMaterial/FoodProperties/Enchantment レジストリ、Placement `MapCodec`、`potionMixes` AT。JEI 非推奨は警告のみ |
| 2026-05-17 | **Thermal Foundation `compileJava` 成功** — worldgen `BootstrapContext`、ルート `HolderLookup` + `Enchantments.FORTUNE` Holder。§9.2 参照 |
| 2026-05-17 | **B-06 / B-07** — Modrinth Maven 追加、`jar` / `build` 検証。Arclight 手順を §11.1 に追記 |
| 2026-05-17 | **再検証** — `compileJava` / `build` / `runData` 成功。CoFH Core（`ArmorMaterialCoFH`・`BlockLootSubProviderCoFH`）と TF-F05/F06 を反映。Arclight A-01〜A-03 は未実施 |

### 12.1 チェックリスト

| 区分 | 状態 |
|------|------|
| B-01〜B-03, B-05〜B-07 | **完了** |
| B-04 neoforge.mods.toml | **完了** |
| N-08 イベント登録 | **完了** |
| S-01〜S-05 ソース調査 | **完了**（N-08 除く） |
| TF-F01〜TF-F06 Foundation データ生成 | **完了** — §9.2（`runData` 含む） |
| CoFH Core CC-R01〜R02 | **完了** — §9.3（`publishToMavenLocal` 要） |
| Thermal Core 依存 | **compileJava 成功** — §9.1 / §9.4 参照 |
| Arclight A-01〜A-03 | **未実施** |

---

*本ドキュメントは Free 枠での作業前提で作成・更新する。*
