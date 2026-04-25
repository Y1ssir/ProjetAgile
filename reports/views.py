import os

from django.shortcuts import render, redirect, get_object_or_404
from django.contrib.auth.decorators import login_required
from django.contrib.auth import login, logout
from django.contrib.auth.forms import UserCreationForm, AuthenticationForm
from django.db.models import Count
from .utils import get_eco_advice
from .forms import ReportForm
from .models import Report

def get_stats():
    total = Report.objects.count()
    resolved = Report.objects.filter(status='RESOLVED').count()
    return {
        'total': total,
        'pending': Report.objects.filter(status='PENDING').count(),
        'in_progress': Report.objects.filter(status='IN_PROGRESS').count(),
        'resolved': resolved,
        'resolution_rate': round((resolved / total * 100), 1) if total > 0 else 0,
    }

def home(request):
    return render(request, 'reports/home.html', {'stats': get_stats()})


def register(request):
    if request.method == 'POST':
        form = UserCreationForm(request.POST)
        if form.is_valid():
            user = form.save()
            login(request, user)
            return redirect('dashboard')
    else:
        form = UserCreationForm()
    return render(request, 'reports/register.html', {'form': form})


@login_required
def dashboard(request):
    from django.db.models import Count
    
    # Stats générales
    stats = {
        'total': Report.objects.count(),
        'pending': Report.objects.filter(status='PENDING').count(),
        'in_progress': Report.objects.filter(status='IN_PROGRESS').count(),
        'resolved': Report.objects.filter(status='RESOLVED').count(),
    }
    
    # Calcul taux de résolution
    if stats['total'] > 0:
        stats['resolution_rate'] = round(stats['resolved'] / stats['total'] * 100, 1)
    else:
        stats['resolution_rate'] = 0

    # Stats par catégorie pour le graphique
    category_dict = dict(Report.CATEGORY_CHOICES)
    category_data = Report.objects.values('category').annotate(count=Count('id'))
    category_stats = [
        {
            'label': category_dict.get(item['category'], item['category'])[:15],
            'count': item['count']
        }
        for item in category_data
    ]

    return render(request, 'reports/dashboard.html', {
        'stats': stats,
        'recent_reports': Report.objects.all().order_by('-created_at')[:10],
        'category_stats': category_stats,
    })

    return render(request, 'reports/dashboard.html', {
        'stats': get_stats(),
        'recent_reports': Report.objects.all().order_by('-created_at')[:10],
        'category_stats': category_stats,
    })


@login_required
def report_create(request):
    if request.method == 'POST':
        form = ReportForm(request.POST, request.FILES)
        if form.is_valid():
            report = form.save(commit=False)
            report.author = request.user
            advice = get_eco_advice(report.category, report.description)
            report.ai_advice = advice 

            report.save()
            
            return redirect('reports:report_success')
    else:
        form = ReportForm()
    return render(request, 'reports/report_form.html', {
        'form': form,
        'stats': get_stats(),
    })


def report_success(request):
    return render(request, 'reports/success.html')


# ============================================================
# LISTE DES SIGNALEMENTS
# ============================================================
def report_list(request):
    reports = Report.objects.all().order_by('-created_at')
    category_filter = request.GET.get('category')
    status_filter = request.GET.get('status')
    if category_filter:
        reports = reports.filter(category=category_filter)
    if status_filter:
        reports = reports.filter(status=status_filter)
    return render(request, 'reports/report_list.html', {
        'reports': reports,
        'categories': Report.CATEGORY_CHOICES,
        'statuses': Report.STATUS_CHOICES,
        'stats': get_stats(),
    })
from django.conf import settings

# ============================================================
# DÉTAIL D'UN SIGNALEMENT
# ============================================================
def report_detail(request, pk):
    import os
    print("Clé API dans la vue :", settings.OPENROUTER_API_KEY)
    print("Clé depuis os.environ :", os.environ.get("OPENROUTER_API_KEY"))
    report = get_object_or_404(Report, pk=pk)
    return render(request, 'reports/report_detail.html', {
        'report': report,
    })