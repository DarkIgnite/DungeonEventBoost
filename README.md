# ⚡ DungeonEventBoost

Plugin Paper / Spigot Minecraft (1.21+) untuk menyelenggarakan **Sistem Event Boost Berkala** (misalnya setiap 6 jam sekali selama 10 menit) serta **Sistem Item Ramuan Boost Personal (Boost Potions)** yang dapat dikonsumsi oleh masing-masing player dengan durasi tersendiri.

---

## ✨ 10 Jenis Event Boost & Ramuan Potion

1. **📦 2x Dungeon Mob Drops (`DROPS`)**
   - Menggandakan item drop dari mob dungeon MythicMobs saat aktif.
   - Kompatibel dengan MMOItems, custom NBT, dan batas `maxStackSize`.

2. **⚔️ 2x AuraSkills EXP (`EXP_SKILLS`)**
   - Mengalikan 2 seluruh perolehan EXP di AuraSkills (Mining, Farming, Fighting, Archery, dll.).

3. **💥 2x Damage ke Mob Dungeon (`DAMAGE_DEALT`)**
   - Serangan pemain (fisik & panah) ke mob dungeon MythicMobs menghasilkan 2x damage lipat lebih sakit.
   - Dilengkapi efek partikel kritis dan suara pukulan khusus.

4. **🛡️ 50% Dungeon Defense Boost (`DEFENSE`)**
   - Damage yang diterima pemain dari mob dungeon terpotong 50% (damage taken x0.5), membuat pertarungan boss lebih aman.
   - Dilengkapi efek suara tameng dan partikel enchanted.

5. **💰 2x Jobs & Economy Boost (`ECONOMY`)**
   - Gaji dari pekerjaan plugin **Jobs** dan uang drop mob MythicMobs digandakan 2x lipat.

6. **✨ 2x Vanilla EXP Boost (`EXP_VANILLA`)**
   - Seluruh bola EXP vanilla dari membunuh mob dan menambang digandakan 2x lipat (cocok untuk enchant gacha).

7. **⚡ Dungeon Berserk / Swiftness (`SWIFTNESS`)**
   - Pemain yang bertarung melawan mob dungeon otomatis memperoleh efek **Speed II & Haste II**.

8. **🔥 MEGA ALL-STAR BOOST (`MEGA_FRENZY`)**
   - Ramuan/Event spesial langka di mana **SELURUH EFEK BOOST DI ATAS AKTIF BERSAMAAN** sekaligus!

9. **🍲 5x Money Koki MBG Boost (`KOKI_MBG_5X`)**
   - Pendapatan uang kasir dari Dapur Koki MBG digandakan 5x lipat.

10. **💎 10x Money Koki MBG Boost (`KOKI_MBG_10X`)**
    - Pendapatan uang kasir dari Dapur Koki MBG digandakan 10x lipat.

---

## 🧪 Fitur Item Ramuan Boost (Boost Potions)

Setiap jenis event boost kini hadir dalam bentuk item ramuan consumable selayaknya potion vanilla:
- **Instant Consume / Drinking**: Klik kanan untuk langsung meminum ramuan (durasi & konsumsi instan dapat diatur di `config.yml`).
- **Personal Duration Timer**: Setiap ramuan memiliki batas waktu/hitung mundur tersendiri yang tersimpan aman pada data pemain (`player_boosts.yml`).
- **Stacking Duration**: Jika meminum ramuan sejenis lagi saat efek masih aktif, durasi otomatis bertambah (`EXTEND`).
- **Dynamic Personal BossBar**: Menampilkan hitung mundur digital dan progres sisa waktu ramuan aktif di bagian atas layar pemain (`🧪 2x Dungeon Drops ⚡ (09:45)`).
- **Visual & Sound Effects**: Dilengkapi warna potion khusus (RGB hex), efek glint kilau enchantment, partikel sihir saat diminum, suara glek minum (`ENTITY_GENERIC_DRINK`) + suara chime aktivasi (`ENTITY_PLAYER_LEVELUP`), serta suara deaktif saat habis.
- **Dukungan Kustomisasi**: Setiap item potion dapat diatur Material, Warna, CustomModelData, DisplayName, dan Lore di `config.yml`.

---

## 📋 Perintah & Izin

| Perintah | Deskripsi | Izin |
| :--- | :--- | :--- |
| `/eb status` | Cek status event global dan hitung mundur berikutnya | `dungeoneventboost.use` |
| `/eb myboost` *(alias: `/eb personal`)* | Cek sisa waktu seluruh ramuan boost personal yang sedang aktif | `dungeoneventboost.use` |
| `/eb give <player> <jenis> [menit] [jumlah]` | Memberikan item ramuan boost ke pemain tertentu | `dungeoneventboost.admin` |
| `/eb giveall <jenis> [menit] [jumlah]` | Membagikan item ramuan boost ke seluruh pemain yang online | `dungeoneventboost.admin` |
| `/eb remove <player> [jenis]` *(alias: `/eb clear`)* | Menghapus efek boost personal tertentu/seluruhnya dari pemain | `dungeoneventboost.admin` |
| `/eb start [jenis] [menit]` | Memulai event global tertentu secara manual | `dungeoneventboost.admin` |
| `/eb stop` | Menghentikan event global yang sedang aktif | `dungeoneventboost.admin` |
| `/eb reload` | Reload file konfigurasi `config.yml` | `dungeoneventboost.admin` |
| `/eb help` | Menampilkan daftar bantuan perintah | `dungeoneventboost.use` |

*Pilihan jenis boost: `drops`, `exp_skills`, `damage`, `defense`, `economy`, `exp_vanilla`, `swiftness`, `all`, `koki_5x`, `koki_10x`.*

---

## 🧩 PlaceholderAPI

| Placeholder | Keterangan | Contoh Output |
| :--- | :--- | :--- |
| `%eventboost_status%` | Status event global | `&aAktif` / `&cTidak Aktif` |
| `%eventboost_event_name%` | Nama tampilan event global aktif | `2x Dungeon Mob Drops` |
| `%eventboost_time_left%` | Sisa durasi event global digital | `09:45` |
| `%eventboost_personal_has_boost%` | Apakah pemain memiliki boost ramuan aktif | `true` / `false` |
| `%eventboost_has_any_boost%` | Apakah ada event global ATAU boost personal aktif | `true` / `false` |
| `%eventboost_personal_boosts%` | Ringkasan semua boost personal aktif pemain | `2x Mob Drops (04:32)` |
| `%eventboost_personal_time_left_<type>%` | Sisa durasi digital ramuan tertentu (cth: `drops`) | `04:32` |
| `%eventboost_personal_time_left_formatted_<type>%` | Sisa durasi lengkap ramuan tertentu | `4 menit 32 detik` |
| `%eventboost_personal_active_<type>%` | Cek apakah ramuan tertentu aktif pada pemain | `true` / `false` |
| `%eventboost_drop_multiplier%` | Pengali drop efektif pemain | `2.0` |
| `%eventboost_exp_multiplier%` | Pengali AuraSkills EXP efektif pemain | `2.0` |
| `%eventboost_damage_multiplier%` | Pengali damage dungeon efektif pemain | `2.0` |
| `%eventboost_defense_multiplier%` | Pengali defense damage taken efektif | `0.5` |
| `%eventboost_economy_multiplier%` | Pengali Jobs payment efektif pemain | `2.0` |
| `%eventboost_koki_multiplier%` | Pengali kasir Dapur Koki MBG efektif pemain | `5.0` / `10.0` |
| `%eventboost_vanilla_exp_multiplier%`| Pengali Vanilla EXP efektif pemain | `2.0` |

---

## 👤 Author
- **DarkIgnite** (ryanfadhila18@gmail.com)
- Server: *Survival Season 7*
