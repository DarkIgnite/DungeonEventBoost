# ⚡ DungeonEventBoost

Plugin Paper / Spigot Minecraft (1.21+) untuk menyelenggarakan **Sistem Event Boost Berkala** (misalnya setiap 6 jam sekali selama 10 menit). Memiliki sistem rotasi / random otomatis untuk berbagai jenis event boost menarik bagi server survival RPG & dungeon.

---

## ✨ 8 Jenis Event Boost

1. **📦 2x Dungeon Mob Drops (`DROPS`)**
   - Menggandakan item drop dari mob dungeon MythicMobs saat event aktif.
   - Kompatibel dengan MMOItems, custom NBT, dan batas `maxStackSize`.

2. **⚔️ 2x AuraSkills EXP (`EXP_SKILLS`)**
   - Mengalikan 2 seluruh perolehan EXP di AuraSkills (Mining, Farming, Fighting, Archery, dll.).

3. **💥 2x Damage ke Mob Dungeon (`DAMAGE_DEALT`)**
   - Serangan pemain (fisik & projectile) ke mob dungeon MythicMobs menghasilkan 2x damage lipat lebih sakit.
   - Dilengkapi efek partikel kritis dan suara pukulan khusus.

4. **🛡️ 50% Dungeon Defense Boost (`DEFENSE`)**
   - Damage yang diterima pemain dari mob dungeon terpotong 50% (damage taken x0.5), membuat pertarungan boss lebih aman.
   - Dilengkapi efek suara tameng dan partikel enchanted.

5. **💰 2x Jobs & Economy Boost (`ECONOMY`)**
   - Gaji dari pekerjaan plugin **Jobs** dan uang drop mob digandakan 2x lipat.

6. **✨ 2x Vanilla EXP Boost (`EXP_VANILLA`)**
   - Seluruh bola EXP vanilla dari membunuh mob dan menambang digandakan 2x lipat (cocok untuk enchant gacha).

7. **⚡ Dungeon Berserk / Swiftness (`SWIFTNESS`)**
   - Pemain yang bertarung melawan mob dungeon otomatis memperoleh efek **Speed II & Haste II**.

8. **🔥 MEGA ALL-STAR BOOST (`MEGA_FRENZY`)**
   - Event spesial langka di mana **SELURUH BOOST DI ATAS AKTIF BERSAMAAN** sekaligus!

---

## 🎨 Tampilan & Desain Chat
- **Native Chat Centering**: Teks dengan tag `<center>...</center>` secara otomatis dihitung lebar font Minecraft-nya dan diposisikan di tengah jendela chat pemain tanpa memunculkan tag mentah `<center>` di layar.
- **Dynamic BossBar**: Warna dan judul BossBar otomatis berubah sesuai jenis event yang sedang aktif dengan countdown digital (`mm:ss`).
- **Peringatan Countdown**: Hitung mundur otomatis di 30m, 15m, 5m, 1m, 30s, 10s, dan 5s..1s disertai suara notifikasi tick.
- **State Persistence**: Sisa countdown tersimpan di `data.yml` sehingga tidak ter-reset saat server direstart.

---

## 📋 Perintah & Izin

| Perintah | Deskripsi | Izin |
| :--- | :--- | :--- |
| `/eb status` | Cek status event aktif / sisa waktu event berikutnya | `dungeoneventboost.use` |
| `/eb start [jenis] [menit]` | Memulai event tertentu secara manual | `dungeoneventboost.admin` |
| `/eb stop` | Menghentikan event yang sedang aktif | `dungeoneventboost.admin` |
| `/eb reload` | Reload file konfigurasi `config.yml` | `dungeoneventboost.admin` |
| `/eb help` | Menampilkan daftar bantuan | `dungeoneventboost.use` |

*Pilihan jenis event untuk `/eb start`: `drops`, `exp_skills`, `damage`, `defense`, `economy`, `exp_vanilla`, `swiftness`, `all`.*

---

## 🧩 PlaceholderAPI

| Placeholder | Keterangan | Contoh Output |
| :--- | :--- | :--- |
| `%eventboost_status%` | Status event berwarna | `&aAktif` / `&cTidak Aktif` |
| `%eventboost_event_type%` | Tipe enum event aktif | `DROPS`, `DEFENSE`, dll. |
| `%eventboost_event_name%` | Nama tampilan event aktif | `2x Dungeon Mob Drops` |
| `%eventboost_time_left%` | Sisa durasi event | `09:45` |
| `%eventboost_time_left_formatted%` | Sisa durasi lengkap | `9 menit 45 detik` |
| `%eventboost_time_until_next%` | Hitung mundur event berikutnya | `05:42:10` |
| `%eventboost_time_until_next_formatted%` | Hitung mundur lengkap | `5 jam 42 menit 10 detik` |
| `%eventboost_drop_multiplier%` | Pengali drop | `2.0` |
| `%eventboost_exp_multiplier%` | Pengali EXP skills | `2.0` |
| `%eventboost_damage_multiplier%` | Pengali damage | `2.0` |
| `%eventboost_defense_multiplier%` | Pengali defense damage taken | `0.5` |
| `%eventboost_economy_multiplier%` | Pengali Jobs payment | `2.0` |
| `%eventboost_vanilla_exp_multiplier%`| Pengali Vanilla EXP | `2.0` |

---

## 👤 Author
- **DarkIgnite** (ryanfadhila18@gmail.com)
- Server: *Survival Season 7*
