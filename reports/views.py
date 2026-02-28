from django.shortcuts import render,redirect,get_object_or_404
from django.contrib.auth.decorators import login_required
from .utils import get_eco_advice
from .forms import ReportForm
from .models import Report

@login_required 
def report_create(request):
    if request.method == 'POST':
        form = ReportForm(request.POST, request.FILES)
        if form.is_valid():
            report = form.save(commit=False)
            report.author = request.user      
            report.save()                     
            
            return redirect('reports:report_success')
    else:
        form = ReportForm()
    
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
    return render(request, 'reports/report_list.html', {
        'reports': reports,
        'categories': categories
    })
def report_detail(request, pk):
    report = get_object_or_404(Report, pk=pk)
    return render(request, 'reports/report_detail.html', {'report': report})
def report_detail(request, pk):
    report = get_object_or_404(Report, pk=pk)
    
    # Appel à l'IA d'OpenRouter
    advice = get_eco_advice(report.get_category_display(), report.description)
    
    return render(request, 'reports/report_detail.html', {
        'report': report,
        'advice': advice
    })