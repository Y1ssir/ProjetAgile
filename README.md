# ProjetAgile
## Installation
Suivez ces étapes pour cloner et lancer le projet sur votre machine :
1. **Cloner le projet**
   ```bash
   git clone https://github.com/Y1ssir/ProjetAgile.git
   ```
   ```bash
   cd ProjetAgile
2. **Créer et activer l'environnement virtuel**
   ```bash
   python -m venv venv
   Sur Windows :
     venv\Scripts\activate
   Sur Mac/Linux :
     source venv/bin/activate
3. **Installer les dépendances**
   ```bash
   pip install -r requirements.txt
4. **Initialiser la base de données locale**
   ```bash
   python manage.py migrate
   python manage.py createsuperuser
   
5. **Lancer le serveur**
   ```bash
   python manage.py runserver

## 👥 Contributors
- **Yassir Jaber** (@Y1ssir)
- **Ammar ELmeliani** (@EL-Star456) [https://github.com/EL-Star456]
