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

## 📊 See Earth in Action!
When you run the system, you'll see something like this:

```text
=== EONET EVENT STATISTICS ===
Update #5 at 17:48:59

Total events: 54

Events by category:
  Wildfires           :  30 [###########         ] 55%
  Sea and Lake Ice    :  23 [########            ] 42%
  Severe Storms       :   1 [                    ] 1%
  Water Color         :   0 [                    ] 0%
  Dust and Haze       :   0 [                    ] 0%
  Snow                :   0 [                    ] 0%
  Manmade             :   0 [                    ] 0%
  Volcanoes           :   0 [                    ] 0%

Data updates every 10 seconds
App will stop in 0:18
==============================
```
## 🌍 Happy hacking — and enjoy watching the planet breathe in real time!

Built with ☕ and curiosity by @agorohovcom