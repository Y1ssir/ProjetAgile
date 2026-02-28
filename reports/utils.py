import requests
import json
from django.conf import settings

def get_eco_advice(category, description):
    prompt = f"En tant qu'expert en écologie, donne un conseil court et concret (max 2 phrases) pour résoudre ce problème sur un campus universitaire. Catégorie: {category}. Problème: {description}."
    
    try:
        response = requests.post(
            url="https://openrouter.ai/api/v1/chat/completions",
            headers={
                "Authorization": f"Bearer {settings.OPENROUTER_API_KEY}",
                "Content-Type": "application/json",
            },
            data=json.dumps({
                "model": "google/gemini-2.0-flash-001", # Modèle gratuit ou peu coûteux
                "messages": [{"role": "user", "content": prompt}]
            })
        )
        data = response.json()
        return data['choices'][0]['message']['content']
    except Exception:
        return "Pensez à éteindre les lumières et à réduire votre consommation d'eau !"