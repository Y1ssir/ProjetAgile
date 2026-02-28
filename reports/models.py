from django.db import models
from django.contrib.auth.models import User

# Create your models here.

class Report(models.Model):
    CATEGORY_CHOICES = [
        ('ENERGY_WASTE', 'Gaspillage énergétique (Lumière/Chauffage)'),
        ('ELEC_ISSUE', 'Problème électrique ou équipement défectueux'),
        ('WATER_LEAK', 'Fuite d’eau ou robinet défectueux'),
        ('WATER_WASTE', 'Gaspillage d’eau'),
        ('WASTE_FULL', 'Poubelle/Bac de tri plein ou débordant'),
        ('DEPOSIT_WILD', 'Dépôt sauvage ou déchet mal trié'),
        ('CLEANLINESS', 'Problème de propreté/hygiène'),
        ('GREEN_SPACE', 'Espaces verts (Arbre dégradé, plante assoiffée)'),
        ('ANIMAL_DANGER', 'Protection de la faune locale'),
        ('ROOM_MISUSE', 'Salle inutilisée restant ouverte/éclairée'),
        ('OTHER', 'Autre signalement environnemental'),
    ]
    STATUS_CHOICES = [
        ('PENDING', 'En attente'),
        ('IN_PROGRESS', 'En cours'),
        ('RESOLVED', 'Résolu'),
    ]

    title = models.CharField(max_length=150, verbose_name="Titre du signalement")
    description = models.TextField(verbose_name="Description du problème")
    category = models.CharField(max_length=20, choices=CATEGORY_CHOICES, default='OTHER')
    location = models.CharField(max_length=255, verbose_name="Localisation (adresse ou GPS)")
    status = models.CharField(max_length=20, choices=STATUS_CHOICES, default='PENDING')
    image = models.ImageField(upload_to='reports/%Y/%m/%d/', verbose_name="Photo du déchet")
    
    created_at = models.DateTimeField(auto_now_add=True, verbose_name="Date de création")
    
    author = models.ForeignKey(User, on_delete=models.CASCADE, related_name="reports")

    def __str__(self):
        return f"{self.title} - {self.get_category_display()}"