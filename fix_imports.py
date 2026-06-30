import os

dir_path = "app/src/main/java/com/agrogoat/app/"

def replace_in_file(filepath):
    with open(filepath, 'r') as file:
        content = file.read()
    
    new_content = content.replace("import com.agrogoat.app.ui.components.GoatLogo", "import com.agrogoat.core.designsystem.components.GoatLogo")
    new_content = new_content.replace("import com.agrogoat.app.ui.components.GoatImage", "import com.agrogoat.core.designsystem.components.GoatImage")
    new_content = new_content.replace("import com.agrogoat.app.ui.components.formatRupiah", "import com.agrogoat.core.designsystem.components.formatRupiah")
    new_content = new_content.replace("import com.agrogoat.app.ui.components.GoatSilhouette", "import com.agrogoat.core.designsystem.components.GoatSilhouette")
    new_content = new_content.replace("import com.agrogoat.app.ui.components.*", "import com.agrogoat.app.ui.components.*\nimport com.agrogoat.core.designsystem.components.*")
    
    if new_content != content:
        with open(filepath, 'w') as file:
            file.write(new_content)
        print(f"Updated {filepath}")

for root, dirs, files in os.walk(dir_path):
    for file in files:
        if file.endswith(".kt"):
            replace_in_file(os.path.join(root, file))
