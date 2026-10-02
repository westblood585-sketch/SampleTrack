# 🧪 Laboratuvar Numune Yönetim Sistemi

Bir laboratuvarın farklı müşterilerden gelen numuneleri kabul etmesini, aşamalardan
geçirmesini ve sonuç üretmesini sağlayan tam yığın (full-stack) bir uygulama.
Backend **Java 17 / Spring Boot 3 / PostgreSQL**, frontend **React / TypeScript / Tailwind CSS**
ile katmanlı mimaride geliştirilmiştir.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
![React](https://img.shields.io/badge/React-19-61DAFB)
![Tests](https://img.shields.io/badge/tests-42%20passing-success)
![Coverage](https://img.shields.io/badge/coverage-%E2%89%A570%25-success)

---

## İçindekiler

- [Genel Bakış](#genel-bakış)
- [Domain Modeli](#domain-modeli)
- [Numune Durum Makinesi](#numune-durum-makinesi)
- [İş Kuralları](#i̇ş-kuralları)
- [Mimari](#mimari)
- [Proje Yapısı](#proje-yapısı)
- [Kurulum ve Çalıştırma](#kurulum-ve-çalıştırma)
- [API Referansı](#api-referansı)
- [Ekranlar](#ekranlar)
- [Test ve Kapsama](#test-ve-kapsama)
- [Kod Kalitesi](#kod-kalitesi)
- [Proje Dosyaları](#proje-dosyaları)

---

## Genel Bakış

Bir numune:
- bir **müşteriye** aittir,
- bir **test listesine** sahiptir,
- kabulden tamamlanmaya kadar farklı **aşamalardan** geçer,
- her test için bir **sonuç** üretir.

Sistem bu akışı uçtan uca yönetir: numune kabulünden test sonucu girişine,
durum geçişlerinden denetim (audit) kaydına kadar.

## Domain Modeli

| Entity | Açıklama |
|---|---|
| `Customer` | Numune gönderen müşteri (klinik/hastane) |
| `TestDefinition` | Test kataloğu (kod, ad, birim, referans aralığı) |
| `Sample` | Kabul edilen numune; bir müşteriye aittir, bir veya daha fazla teste sahiptir |
| `SampleTest` | Numune ile test tanımı arasındaki ilişki; kendi durumu ve (varsa) sonucu vardır |
| `TestResult` | Bir `SampleTest`e girilen ölçüm sonucu |
| `SampleStageHistory` | Numunenin geçtiği her durum değişikliğinin denetim kaydı |

ER diyagramı: [`lab-sample/lab-sample-erd.drawio`](./lab-sample/lab-sample-erd.drawio)
([app.diagrams.net](https://app.diagrams.net) ile açılabilir).

## Numune Durum Makinesi

```
RECEIVED → IN_PROGRESS → COMPLETED
    │           │
    └───────────┴──→ REJECTED
```

Geçişler `SampleStateMachine` sınıfında merkezi olarak tanımlıdır.
`COMPLETED` ve `REJECTED` terminal durumlardır; buradan başka bir duruma geçiş yapılamaz.

## İş Kuralları

| # | Kural | Uygulandığı yer |
|---|---|---|
| 1 | Aynı barkod iki kez oluşturulamaz | `sample.barcode` unique kısıtı + servis ön kontrolü |
| 2 | Reddedilen numuneye sonuç girilemez | `SampleService.enterResult` |
| 3 | Tüm testler tamamlanmadan numune COMPLETED olamaz | `SampleService.complete` |
| 4 | Sonuç girildikten sonra TestDefinition değiştirilemez | `SampleTest.testDefinition` (`updatable=false`) + `TestDefinitionService.update` |

## Mimari

**Backend** — katmanlı mimari:

```
controller  → HTTP, DTO doğrulama, Swagger anotasyonları
service     → iş kuralları, durum makinesi, transaction sınırı
repository  → Spring Data JPA (Derived Query / JPQL, native SQL yok)
entity      → JPA @Entity (API dışına açılmaz)
dto         → record tabanlı istek/yanıt sözleşmeleri
mapper      → MapStruct (entity ↔ dto)
exception   → @RestControllerAdvice ile standart hata DTO'su
```

Hata yanıtları tek bir formatta döner (`ErrorResponse`): zaman damgası, HTTP durumu,
makine-okunur hata kodu, mesaj, istek yolu ve varsa alan bazlı doğrulama hataları.

**Frontend** — sayfa/bileşen/servis ayrımı:

```
pages       → her ekran için bir sayfa bileşeni
components  → tekrar kullanılabilir UI parçaları (StatusBadge, Modal, Stepper...)
api         → backend endpoint'lerini saran ince istemci fonksiyonları
lib         → fetch wrapper, biçimlendirme yardımcıları
types       → backend DTO'larıyla birebir eşleşen TypeScript tipleri
```

## Proje Yapısı

```
.
├── lab-sample/              # Backend (Spring Boot)
│   ├── src/main/java/com/lab/sample/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   ├── dto/
│   │   ├── mapper/
│   │   ├── exception/
│   │   └── config/
│   ├── src/test/java/...
│   ├── lab-sample-erd.drawio
│   ├── docker-compose.yml
│   ├── Dockerfile
│   ├── .env.example
│   └── pom.xml
└── lab-sample-ui/            # Frontend (React + Vite)
    └── src/
        ├── pages/
        ├── components/
        ├── api/
        └── types/
```

## Kurulum ve Çalıştırma

### Gereksinimler
- Java 17
- Node.js 18+
- Docker ve Docker Compose

### 0. Ortam değişkenlerini ayarla

Veritabanı kimlik bilgileri repoda **sabit kodlanmamıştır**. İlk kurulumda:

```bash
cd lab-sample
cp .env.example .env
```

`.env` dosyasını açıp `POSTGRES_PASSWORD` değerini kendi şifrenle değiştir.
`.env` dosyası `.gitignore` içinde olduğu için GitHub'a gönderilmez.

### 1. Veritabanını başlat
```bash
docker compose up -d postgres
```

### 2. Backend'i çalıştır
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=seed
```
`seed` profili örnek müşteri ve test kataloğu ile başlatır (opsiyonel, kaldırılabilir).
API: `http://localhost:8080` · Swagger UI: `http://localhost:8080/swagger-ui.html`

### 3. Frontend'i çalıştır
```bash
cd ../lab-sample-ui
npm install
npm run dev
```
Arayüz: `http://localhost:5173`

### Alternatif: Tam ortamı Docker ile ayağa kaldırma
```bash
cd lab-sample
docker compose up --build
```
`app` servisi PostgreSQL'e bağımlı olarak (`depends_on: service_healthy`) başlar ve
aynı `.env` dosyasındaki kimlik bilgilerini kullanır.

## API Referansı

| Yöntem | Yol | Açıklama |
|---|---|---|
| `POST` | `/api/customers` | Müşteri oluştur |
| `GET` | `/api/customers/{id}` | Müşteri detayı |
| `GET` | `/api/customers` | Müşterileri sayfalı listele |
| `POST` | `/api/test-definitions` | Test tanımı oluştur |
| `PUT` | `/api/test-definitions/{id}` | Test tanımını güncelle |
| `PATCH` | `/api/test-definitions/{id}/active` | Aktif/pasif yap |
| `GET` | `/api/test-definitions` | Test tanımlarını listele |
| `POST` | `/api/samples` | Numune kabul et |
| `GET` | `/api/samples/{id}` | Numune detayı (testler, sonuçlar, geçmiş) |
| `GET` | `/api/samples` | Numuneleri filtreli listele (`status`, `customerId`, `barcode`) |
| `POST` | `/api/samples/{id}/start` | RECEIVED → IN_PROGRESS |
| `POST` | `/api/samples/{id}/reject` | Numuneyi reddet (gerekçe zorunlu) |
| `POST` | `/api/samples/{id}/complete` | Numuneyi tamamla |
| `POST` | `/api/samples/{sampleId}/tests/{sampleTestId}/result` | Test sonucu gir |

Tüm endpoint ve DTO alanları Swagger UI üzerinden detaylı açıklamalarla görülebilir.

## Ekranlar

| Ekran | İçerik |
|---|---|
| **Panel** | Durum bazlı özet kartları, son numuneler |
| **Numuneler** | Durum + barkod filtresi, sayfalı liste |
| **Numune Kabul Et** | Müşteri seçimi + çoklu test seçimiyle numune oluşturma |
| **Numune Detayı** | Durum stepper'ı, satır içi sonuç girişi, referans aralığı uyarıları, aşama geçmişi, aksiyon butonları |
| **Test Tanımları** | Katalog yönetimi, referans aralığı, aktif/pasif geçişi |
| **Müşteriler** | Liste ve düzenleme |

## Test ve Kapsama

```bash
cd lab-sample
./mvnw verify
```

- **Unit testler**: servis katmanı (Mockito), durum makinesi, referans aralığı hesaplayıcı — veritabanı gerektirmez.
- **Integration testler**: repository katmanı ve uçtan uca (MockMvc) senaryolar, Testcontainers ile gerçek PostgreSQL üzerinde çalışır (Docker gereklidir).
- **JaCoCo**: `./mvnw verify` sonunda `target/site/jacoco/index.html` raporu üretir; kapsama %70'in altındaysa build başarısız olur.

**Son durum:** 42/42 test başarılı, toplam satır kapsaması **%72**.

## Kod Kalitesi

Proje SonarLint ile taranmıştır; **Blocker, Critical veya Major seviyesinde bulgu bulunmamaktadır.**

## Proje Dosyaları

- `lab-sample/lab-sample-erd.drawio` — veritabanı ER modeli
- `lab-sample/docker-compose.yml` — PostgreSQL + opsiyonel uygulama servisi (kimlik bilgileri `.env`'den okunur)
- `lab-sample/.env.example` — örnek ortam değişkenleri şablonu (gerçek şifre içermez)
- `lab-sample/Dockerfile` — çok aşamalı (multi-stage) uygulama imajı
- `lab-sample-ui/` — React frontend kaynak kodu
