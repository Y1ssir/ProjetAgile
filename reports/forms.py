from django import forms
from .models import Report

class ReportForm(forms.ModelForm):
    class Meta:
        model = Report
        fields = ['title', 'description', 'category', 'image']
        
        widgets = {
            'description': forms.Textarea(attrs={'rows': 4, 'placeholder': 'Décrivez le problème...'}),
            'location': forms.TextInput(attrs={'placeholder': 'Ex: Bâtiment B, 2ème étage'}),
        }