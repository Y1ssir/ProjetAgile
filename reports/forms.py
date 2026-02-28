from django import forms
from .models import Report

class ReportForm(forms.ModelForm):
    class Meta:
        model = Report
        # On définit les champs que l'étudiant doit remplir
        fields = ['title', 'description', 'category', 'location', 'image']
        
        # Optionnel : Ajoute des classes CSS pour ton binôme Front-end (ex: Bootstrap)
        widgets = {
            'description': forms.Textarea(attrs={'rows': 4, 'placeholder': 'Décrivez le problème...'}),
            'location': forms.TextInput(attrs={'placeholder': 'Ex: Bâtiment B, 2ème étage'}),
        }