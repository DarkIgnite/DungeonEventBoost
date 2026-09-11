# ⚡ DungeonEventBoost

Plugin Paper / Spigot Minecraft (1.21+) untuk menyelenggarakan **Event Boost Berkala** (misalnya setiap 6 jam sekali selama 10 menit) yang meningkatkan hasil grinding dungeon dan skills di server.

---

## ✨ Fitur Utama

1. **2x MythicMobs Dungeon Drops**
   - Menggandakan drop item dari mob dungeon MythicMobs saat event aktif.
   - Mendukung filter Faction (misal `Faction: Dungeon`), Mob Types, ataupun seluruh MythicMobs.
   - Kompatibel penuh dengan custom NBT item, MMOItems, dan batas maksimal stack (`maxStackSize`).

2. **2x AuraSkills EXP Boost**
   - Mengalikan 2 seluruh perolehan EXP di plugin AuraSkills selama event berlangsung.
   - Berlaku untuk seluruh aktivitas (Farming, Mining, Foraging, Fighting, Archery, Fishing, dll.) atau dapat difilter per skill.

3. **2x Damage Dealt ke Mob Dungeon**
   - Meningkatkan damage serangan pemain (melee & panah/projectile) sebesar 2x lipat terhadap mob dungeon MythicMobs.
   - Dilengkapi efek visual partikel dan efek suara hantaman kustom.

4. **BossBar & Sistem Pengumuman Interaktif**
   - BossBar dinamis dengan penghitung waktu mundur (`mm:ss`) dan progress bar yang menurun seiring berjalannya event.
   - Broadcast chat, title & subtitle di layar, serta suara notifikasi saat event dimulai dan berakhir.
   - Peringatan hitung mundur sebelum event dimulai (30m, 15m, 5m, 1m, 30s, 10s, 5s - 1s dengan suara detak pling).

5. **Penyimpanan Status (State Persistence)**
   - Timer countdown otomatis tersimpan di `data.yml` agar tidak ter-reset saat server direstart atau crash.

6. **Integrasi PlaceholderAPI**
   - Tersedia berbagai placeholder untuk scoreboard, TAB, atau hologram.

---

## 📋 Perintah & Izin

| Perintah | Deskripsi | Izin |
| :--- | :--- | :--- |
| `/eb status` | Menampilkan status event dan sisa waktu | `dungeoneventboost.use` |
| `/eb start [menit]` | Memulai event boost secara manual | `dungeoneventboost.admin` |
| `/eb stop` | Menghentikan event boost yang sedang aktif | `dungeoneventboost.admin` |
| `/eb reload` | Memuat ulang `config.yml` | `dungeoneventboost.admin` |
| `/eb help` | Menampilkan daftar bantuan perintah | `dungeoneventboost.use` |

*Alias perintah:* `/dungeoneventboost`, `/eventboost`, `/deb`, `/eb`

---

## 🧩 PlaceholderAPI

| Placeholder | Deskripsi | Contoh Output |
| :--- | :--- | :--- |
| `%eventboost_status%` | Status event berwarna | `&aAktif` / `&cTidak Aktif` |
| `%eventboost_is_active%` | Status boolean | `true` / `false` |
| `%eventboost_time_left%` | Sisa durasi event aktif | `09:45` |
| `%eventboost_time_left_formatted%` | Sisa durasi dalam teks lengkap | `9 menit 45 detik` |
| `%eventboost_time_until_next%` | Waktu menuju event berikutnya | `05:42:10` |
| `%eventboost_time_until_next_formatted%` | Waktu menuju event berikutnya lengkap | `5 jam 42 menit 10 detik` |
| `%eventboost_drop_multiplier%` | Pengali drop | `2.0` |
| `%eventboost_exp_multiplier%` | Pengali EXP skills | `2.0` |
| `%eventboost_damage_multiplier%` | Pengali damage | `2.0` |

---

## ⚙️ Kompilasi

```bash
mvn clean package
```
File jar yang sudah di-shade dan siap pakai akan berada di `target/DungeonEventBoost-1.0.0.jar`.

---

## 👤 Author
- **DarkIgnite** (ryanfadhila18@gmail.com)
- Server: *Survival Season 7*
