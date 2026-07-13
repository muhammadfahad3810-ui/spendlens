import numpy as np
import pandas as pd
import tensorflow as tf
from sklearn.model_selection import train_test_split

DIM = 1024          # feature vector size
SEED = 42

CATEGORIES = ["Groceries", "Food", "Transport", "Fuel",
              "Shopping", "Utilities", "Health", "Other"]

def trigram_hash_vector(text: str) -> np.ndarray:
    """Hash character trigrams into a fixed-size bag vector.
    MUST stay identical to the Kotlin implementation."""
    v = np.zeros(DIM, dtype=np.float32)
    t = "  " + text.lower().strip() + "  "   # pad so short names still get trigrams
    for i in range(len(t) - 2):
        tri = t[i:i+3]
        h = 0
        for ch in tri:
            h = (h * 31 + ord(ch)) % 2147483647
        v[h % DIM] += 1.0
    # L2 normalize
    norm = np.linalg.norm(v)
    return v / norm if norm > 0 else v

# ---- Load data ----
df = pd.read_csv("merchants.csv")
X = np.stack([trigram_hash_vector(m) for m in df["merchant"]])
y = np.array([CATEGORIES.index(c) for c in df["category"]])

X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.15, random_state=SEED, stratify=y
)

# ---- Model ----
tf.random.set_seed(SEED)
model = tf.keras.Sequential([
    tf.keras.layers.Input(shape=(DIM,)),
    tf.keras.layers.Dense(128, activation="relu"),
    tf.keras.layers.Dropout(0.3),
    tf.keras.layers.Dense(64, activation="relu"),
    tf.keras.layers.Dense(len(CATEGORIES), activation="softmax"),
])
model.compile(optimizer="adam",
              loss="sparse_categorical_crossentropy",
              metrics=["accuracy"])

model.fit(X_train, y_train, epochs=25, batch_size=64,
          validation_data=(X_test, y_test), verbose=2)

loss, acc = model.evaluate(X_test, y_test, verbose=0)
print(f"\nTest accuracy: {acc:.1%}")

# ---- Sanity check on real examples ----
for name in ["ALMIRAH LUCKY ONE MALL", "IHamsafar Gasoline Serviee Station",
             "KFC DHA", "Careem", "shell f-10", "chughtai lab"]:
    pred = model.predict(trigram_hash_vector(name)[None, :], verbose=0)[0]
    print(f"{name!r:45s} -> {CATEGORIES[int(pred.argmax())]} ({pred.max():.0%})")

# ---- Export TFLite ----
converter = tf.lite.TFLiteConverter.from_keras_model(model)
converter.optimizations = [tf.lite.Optimize.DEFAULT]   # quantize: ~4x smaller
tflite_model = converter.convert()
with open("merchant_classifier.tflite", "wb") as f:
    f.write(tflite_model)

import os
print(f"\nSaved merchant_classifier.tflite "
      f"({os.path.getsize('merchant_classifier.tflite') / 1024:.0f} KB)")
