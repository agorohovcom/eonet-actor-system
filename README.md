# 🚀 EONET Actor System - Live Earth Monitor

> **See Earth’s pulse in real time!** A reactive console app that streams natural events from NASA EONET using a pure Java Actor Model.

![Demo](https://img.shields.io/badge/Status-Live%20Demo-brightgreen)
![Java](https://img.shields.io/badge/Java-21-%23ED8B00)
![Actors](https://img.shields.io/badge/Architecture-Actor%20Model-blueviolet)

## 🌟 What's This?

A lightweight system that pulls live global event data — wildfires, storms, volcanoes, ice changes — and displays it directly in your terminal.

Powered by the Actor Model, so you get clean concurrency without threads, locks, or headaches. 🎭

## 🎬 Quick Start

```bash
git clone https://github.com/agorohovcom/eonet-actor-system.git
cd eonet-actor-system
./gradlew run
```

### Watch as your console comes alive with Earth's activity! 🌍

## 🤔 Why Actors?

Actors handle concurrency the simple, safe way:
* Each actor has its own state
* No shared memory
* Communication only through immutable messages
* Zero race conditions

```java
// Each actor is like a mini-program with its own state
public class EventProcessorActor implements Actor {
private Map<String, Integer> counts = new HashMap<>(); // 🎉 No synchronization needed!

    @Override
    public void onMessage(Object message) {
        // ✅ Only one message processed at a time
        // ✅ No race conditions ever!
        if (message instanceof NewEvent event) {
            counts.merge(event.type(), 1, Integer::sum);
        }
    }
}
```

## 📊 See Earth in Action!
When you run the system, you'll see something like this:

```text
=== EONET EVENT STATISTICS ===
Update #12 at 14:30:25

Total events: 67

Events by category:
Wildfires         :  35 [####################] 52%
Sea and Lake Ice  :  22 [##############      ] 32%
Severe Storms     :   8 [#####               ] 11%
Volcanoes         :   2 [#                   ] 2%
Dust and Haze     :   0 [                    ] 0%

Data updates every 10 seconds
==============================
```
## 🌍 Happy hacking — and enjoy watching the planet breathe in real time!

Built with ☕ and curiosity by @agorohovcom