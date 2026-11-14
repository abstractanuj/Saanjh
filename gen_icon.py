import random
import math

size = 24
center_x = 12
center_y = 12
radius = 8

path_data = []

# Generate many dots
for _ in range(120):
    # Random angle and distance
    angle = random.uniform(0, 2 * math.pi)

    # Use gaussian or random distribution for cloud shape
    r = random.gauss(0, radius / 2)
    # keep inside boundaries
    if r > radius: r = radius
    if r < -radius: r = -radius

    x = center_x + r * math.cos(angle)
    y = center_y + r * math.sin(angle)

    # Optional: stretch horizontally for a cloud
    x = center_x + (x - center_x) * 1.2

    # Small dot radius
    dot_r = random.uniform(0.2, 0.6)

    # Add a circle to path data
    path_data.append(f'M{x-dot_r:.2f},{y:.2f}')
    path_data.append(f'A{dot_r:.2f},{dot_r:.2f} 0 1,1 {x+dot_r:.2f},{y:.2f}')
    path_data.append(f'A{dot_r:.2f},{dot_r:.2f} 0 1,1 {x-dot_r:.2f},{y:.2f}')

xml = f'''<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#FFFFFF"
        android:pathData="{' '.join(path_data)}" />
</vector>'''

with open('C:/Users/Anuj/Downloads/Escape-Launcher-refactor/Escape-Launcher-refactor/core/ui/src/main/res/drawable/ic_khoj_road.xml', 'w') as f:
    f.write(xml)
