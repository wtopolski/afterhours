# Light-Dependent USB LED Circuit – Analysis and Explanation

## 1. What this circuit is

This is a **USB-powered light-dependent LED driver**.

In simple terms:
- The circuit is powered from **USB-C (5 V VBUS)**
- A **photoresistor (LDR)** senses ambient light
- A **BC546B NPN transistor** acts as a switch/amplifier
- An **LED turns ON or OFF depending on light level**

Functionally, this is a **dark-activated LED** (night-light type circuit).

---

## 2. Components used

### Power
- **USB-C connector (PiHut USBC Board usb07b)**
  - Only **VBUS (5 V)** and **GND** are used
  - D+, D−, CC pins are unused

---

### Active component
- **Q1 – BC546B (NPN transistor)**
  - Used as a **low-side switch**
  - Pins:
    - **C (Collector)** → connected to +5 V via R1
    - **B (Base)** → controlled by LDR + R1
    - **E (Emitter)** → drives LED through R2 to ground

---

### Passive components
- **Photoresistor (LDR)**
  - Resistance decreases when light increases
  - Resistance increases in darkness

- **R1 = 10 kΩ**
  - Base bias resistor
  - Limits base current

- **R2 = 220 Ω**
  - LED current-limiting resistor

- **LED**
  - Visual indicator
  - Turns on in low-light conditions

---

## 3. Functional connections

### Base control network
- R1 (10 kΩ) and LDR form a **voltage divider**
- Divider output goes to the **base of BC546B**

### Output stage
- Transistor emitter → R2 → LED → GND
- When the transistor conducts, the LED lights up

---

## 4. How the circuit works

### ☀️ Bright environment (light present)

**Photoresistor resistance is LOW**

- Most voltage drops across R1
- Base voltage is **too low** to forward-bias the transistor

**Result:**
- Base-emitter voltage **V<sub>BE</sub> < 0.7 V**
- Transistor is **OFF**
- No LED current
- **LED is OFF**

---

### 🌙 Dark environment (low light)

**Photoresistor resistance is HIGH**

- Base voltage rises
- Base-emitter junction becomes forward biased

**Result:**
- **V<sub>BE</sub> ≈ 0.7 V**
- Transistor enters **saturation**
- Current flows through LED
- **LED turns ON**

---

## 5. Ohm’s Law in this circuit

### LED current calculation (approximate)

Assumptions:
- USB VBUS = **5 V**
- LED forward voltage ≈ **2.0 V**
- Transistor saturation voltage ≈ **0.2 V**

Voltage across R2:
V_R2 = 5.0 V − 2.0 V − 0.2 V = 2.8 V


LED current:
I_LED = V / R = 2.8 V / 220 Ω ≈ 12.7 mA

✔ Safe LED operating current  
✔ Within BC546B limits  

---

### Base current estimation

Assuming base voltage ≈ **1.2 V** in darkness:
I_B = (1.2 V − 0.7 V) / 10 kΩ ≈ 0.05 mA


With transistor gain β ≈ 200:
I_C ≈ 10 mA


✔ Sufficient to drive the LED

---

## 6. Kirchhoff’s Laws applied

### Kirchhoff’s Voltage Law (KVL)

For the LED loop:
VBUS − V_CE − V_LED − V_R2 = 0

This confirms correct voltage distribution.

---

### Kirchhoff’s Current Law (KCL)

At the transistor junction:
I_E = I_C + I_B

The base current controls the collector current — standard BJT behavior.

---

## 7. PCB design observations

### 2D PCB
- Clear silkscreen labeling
- Proper through-hole spacing
- USB-C breakout footprint used correctly

### 3D PCB
- Good mechanical clearances
- LED and LDR positioned for light exposure
- Correct transistor orientation

✔ Suitable for:
- USB night-light
- Light-sensor demonstrations
- Educational electronics projects

---

## 8. Summary

- ✔ Simple, robust analog design
- ✔ Demonstrates:
  - Voltage dividers
  - Transistor switching
  - Ohm’s Law
  - Kirchhoff’s Laws
- ✔ Safe, low-voltage USB operation
- ✔ Clean PCB implementation

**Possible extensions:**
- Sensitivity adjustment
- Inverted behavior (LED on in light)
- Hysteresis (Schmitt trigger)
- Microcontroller interface

---

![image](https://github.com/wtopolski/afterhours/blob/main/01_dark_detector/easyeda.png)
![image](https://github.com/wtopolski/afterhours/blob/main/01_dark_detector/pcb_render_2d.png)
![image](https://github.com/wtopolski/afterhours/blob/main/01_dark_detector/pcb_render_3d.png)
![image](https://github.com/wtopolski/afterhours/blob/main/01_dark_detector/PXL_20260122_115232464.png)
![image](https://github.com/wtopolski/afterhours/blob/main/01_dark_detector/PXL_20260122_115250926.png)
![image](https://github.com/wtopolski/afterhours/blob/main/01_dark_detector/PXL_20260207_194341496.png)

