<div align="center">

# 🧪 SampleTrack

**Laboratuvar numune kabul, işleme ve sonuç yönetim sistemi**

Bir laboratuvarın farklı müşterilerden gelen numuneleri kabul etmesini, aşamalardan
geçirmesini ve test sonuçları üretmesini uçtan uca yöneten tam yığın (full-stack) uygulama.

![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F?logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)
![React](https://img.shields.io/badge/React-19-61DAFB?logo=react&logoColor=white)
![TypeScript](https://img.shields.io/badge/TypeScript-5-3178C6?logo=typescript&logoColor=white)
![Tailwind](https://img.shields.io/badge/Tailwind-3-06B6D4?logo=tailwindcss&logoColor=white)
![Tests](https://img.shields.io/badge/tests-42%20passing-success)
![Coverage](https://img.shields.io/badge/coverage-%E2%89%A572%25-success)
![SonarLint](https://img.shields.io/badge/SonarLint-clean-success)

</div>

---

## İçindekiler

- [Genel Bakış](#genel-bakış)
- [Özellikler](#özellikler)
- [Domain Modeli](#domain-modeli)
- [Numune Durum Makinesi](#numune-durum-makinesi)
- [İş Kuralları](#iş-kuralları)
- [Mimari](#mimari)
- [Proje Yapısı](#proje-yapısı)
- [Hızlı Başlangıç](#hızlı-başlangıç)
- [Ortam Değişkenleri](#ortam-değişkenleri)
- [Logging & Observability](#logging--observability)
- [API Referansı](#api-referansı)
- [Ekranlar](#ekranlar)
- [Test ve Kapsama](#test-ve-kapsama)
- [Kod Kalitesi](#kod-kalitesi)
- [Proje Dosyaları](#proje-dosyaları)
- [Lisans](#lisans)

---

## Genel Bakış

SampleTrack, bir laboratuvarın numune yaşam döngüsünü baştan sona dijitalleştirir.
Her numune:

- bir **müşteriye** aittir,
- bir **test listesine** sahiptir,
- kabulden tamamlanmaya kadar farklı **aşamalardan** geçer,
- her test için bir **sonuç** üretir.

Sistem bu akışı hem güçlü bir REST API ile hem de kullanıcı dostu bir web arayüzüyle yönetir.

## Özellikler

- 📋 Müşteri ve test kataloğu yönetimi (referans aralıklı)
- 🏷️ Benzersiz barkodla numune kabulü, çoklu test ataması
- 🔄 Durum makinesiyle yönetilen numune akışı (kabul → işlemde → tamamlandı / reddedildi)
- 🧾 Referans aralığına göre otomatik sonuç etiketleme (normal / düşük / yüksek)
- 📜 Her durum değişikliği için denetim (audit) kaydı
- 📑 Swagger/OpenAPI ile tam dokümante edilmiş API
- 🔍 Lombok `@Slf4j` ile yapılandırılmış loglama ve KVKK/GDPR uyumu
- ✅ %72 test kapsamı, Sonar temiz kod tabanı

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

RECEIVED → IN_PROGRESS → COMPLETED
│           │
└───────────┴──→ REJECTED


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

controller  → HTTP, DTO doğrulama, Swagger anotasyonları, @Slf4j info logları
service     → iş kuralları, durum makinesi, kilit/filtreleme debug logları
repository  → Spring Data JPA (Derived Query / JPQL, native SQL yok)
entity      → JPA @Entity (API dışına açılmaz)
dto         → record tabanlı istek/yanıt sözleşmeleri
mapper      → MapStruct (entity ↔ dto)
exception   → @RestControllerAdvice ile standart hata DTO'su (warn/error logları)


Hata yanıtları tek bir formatta döner (`ErrorResponse`): zaman damgası, HTTP durumu,
makine-okunur hata kodu, mesaj, istek yolu ve varsa alan bazlı doğrulama hataları.

**Frontend** — sayfa/bileşen/servis ayrımı:

pages       → her ekran için bir sayfa bileşeni
components  → tekrar kullanılabilir UI parçaları (StatusBadge, Modal, Stepper...)
api         → backend endpoint'lerini saran ince istemci fonksiyonları
lib         → fetch wrapper, biçimlendirme yardımcıları
types       → backend DTO'larıyla birebir eşleşen TypeScript tipleri


## Proje Yapısı

.
├── lab-sample/              # Backend (Spring Boot 3 + PostgreSQL)
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
└── lab-sample-ui/            # Frontend (React + Vite + TypeScript)
└── src/
├── pages/
├── components/
├── api/
└── types/


## Hızlı Başlangıç

### Gereksinimler

- Java 17
- Node.js 18+
- Docker ve Docker Compose

### 1. Ortam değişkenlerini ayarla

Veritabanı kimlik bilgileri repoda **sabit kodlanmamıştır**.

```bash
cd lab-sample
cp .env.example .env