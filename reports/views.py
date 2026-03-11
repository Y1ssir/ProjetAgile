from django.shortcuts import render, redirect, get_object_or_404
from django.contrib.auth.decorators import login_required
from .utils import get_eco_advice
from .forms import ReportForm
from .models import Report

@login_required 
def report_create(request):
    if request.method == 'POST':
        form = ReportForm(request.POST, request.FILES)  # request.FILES pour l'image
        if form.is_valid():
            report = form.save(commit=False)  # Ne sauvegarde pas encore
            report.author = request.user      # Ajoute l'auteur connecté
            report.save()                     # Maintenant sauvegarde
            return redirect('reports:report_success')
        # Si form invalide, on retourne le form avec les erreurs
    else:
        form = ReportForm()  # Formulaire vide pour GET
    
    return render(request, 'reports/report_form.html', {'form': form})


def report_success(request):
    return render(request, 'reports/success.html')


def report_list(request):
    reports = Report.objects.all().order_by('-created_at')
    category_filter = request.GET.get('category')
    status_filter = request.GET.get('status')
    
    if category_filter:
        reports = reports.filter(category=category_filter)
    if status_filter:
        reports = reports.filter(status=status_filter)
    
    categories = Report.CATEGORY_CHOICES
    statuses = Report.STATUS_CHOICES  # Ajout pour le filtre status
    
    return render(request, 'reports/report_list.html', {
        'reports': reports,
        'categories': categories,
        'statuses': statuses,
    })


# ✅ UNE SEULE définition de report_detail (bug corrigé : tu en avais 2 !)
def report_detail(request, pk):
    report = get_object_or_404(Report, pk=pk)
    advice = get_eco_advice(report.get_category_display(), report.description)
    return render(request, 'reports/report_detail.html', {
        'report': report,
        'advice': advice,
    })