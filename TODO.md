# Планы переделать под универсальную акторную архитектуру

## 1. Системный пакет (io.github.agorohovcom.eonet.system)

* SystemActor - базовый системный актор
* SchedulerActor - универсальный планировщик
* SupervisorActor - супервизор для управления ошибками
* CleanupActor - актор очистки (вместо фоновой задачи)
* MonitoringActor - мониторинг состояния системы

## 2. Пользовательский пакет (io.github.agorohovcom.eonet.app)

* EONETOrchestrator - оркестратор для EONET задачи
* EONETPollerActor - опрос NASA API
* EventProcessorActor - обработка событий
* DashboardActor - дашборд статистики

## 3. Для криптовалют (io.github.agorohovcom.crypto.app)

* CryptoOrchestrator - оркестратор для крипто-задачи
* PriceMonitorActor - мониторинг цен
* AlertActor - обработка алертов
* TradeAnalysisActor - анализ торгов

## Преимущества такого подхода:
✅ Переиспользуемая основа - системные акторы общие для всех задач

✅ Изоляция - каждая задача в своем пакете

✅ Гибкость - легко добавлять новые типы задач

✅ Тестируемость - можно тестировать системные и пользовательские акторы отдельно

## Пример использования:

```java
// EONET задача
EONETOrchestrator eonet = new EONETOrchestrator(config);
eonet.start();

// Крипто задача  
CryptoOrchestrator crypto = new CryptoOrchestrator(config);
crypto.start();
```

Так можно будет использовать одну акторную систему для множества разных задач.