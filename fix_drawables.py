import os

dir_path = "app/src/main/java/com/agrogoat/app/"

drawables = ["burawa", "etawa", "kacang", "logo", "banner_agro_goat"]

def replace_in_file(filepath):
    with open(filepath, 'r') as file:
        content = file.read()
    
    new_content = content
    for d in drawables:
        # replace R.drawable.X with com.agrogoat.core.designsystem.R.drawable.X
        new_content = new_content.replace(f"R.drawable.{d}", f"com.agrogoat.core.designsystem.R.drawable.{d}")
    
    if new_content != content:
        with open(filepath, 'w') as file:
            file.write(new_content)
        print(f"Updated {filepath}")

for root, dirs, files in os.walk(dir_path):
    for file in files:
        if file.endswith(".kt"):
            replace_in_file(os.path.join(root, file))
