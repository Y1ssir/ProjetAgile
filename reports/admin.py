from django.contrib import admin
from django.contrib.admin import AdminSite
from django.utils.html import format_html
from django.urls import path
from django.shortcuts import render
from django.db.models import Count
from .models import Report


# ============================================================
# ADMIN SITE PERSONNALISÉ
# ============================================================
class CustomAdminSite(AdminSite):
    site_header = "🌱 Campus Éco-IA — Administration"
    site_title = "Campus Éco-IA Admin"
    index_title = "Tableau de bord"

    def get_urls(self):
        urls = super().get_urls()
        custom_urls = [
            path('dashboard/', self.admin_view(self.dashboard_view), name='dashboard'),
        ]
        return custom_urls + urls

    def dashboard_view(self, request):
        stats = {
            'total': Report.objects.count(),
            'pending': Report.objects.filter(status='PENDING').count(),
            'in_progress': Report.objects.filter(status='IN_PROGRESS').count(),
            'resolved': Report.objects.filter(status='RESOLVED').count(),
        }
        category_data = Report.objects.values('category').annotate(count=Count('id'))
        recent = Report.objects.all().order_by('-created_at')[:10]
        return render(request, 'admin/dashboard.html', {
            'stats': stats,
            'category_data': list(category_data),
            'recent_reports': recent,
        })


# ✅ IMPORTANT : créer l'instance et remplacer l'admin par défaut
custom_admin = CustomAdminSite(name='custom_admin')


# ============================================================
# REPORT ADMIN
# ============================================================
class ReportAdmin(admin.ModelAdmin):
    list_display = ('title', 'category', 'author', 'statut_badge', 'created_at', 'changer_statut')
    list_filter = ('status', 'category', 'created_at')
    search_fields = ('title', 'description', 'location')
    readonly_fields = ('created_at', 'author')
    list_per_page = 20

    def statut_badge(self, obj):
        colors = {
            'PENDING': '#E67E22',
            'IN_PROGRESS': '#2980B9',
            'RESOLVED': '#27AE60',
        }
        labels = {
            'PENDING': '⏳ En attente',
            'IN_PROGRESS': '🔄 En cours',
            'RESOLVED': '✅ Résolu',
        }
        color = colors.get(obj.status, '#999')
        label = labels.get(obj.status, obj.status)
        return format_html(
            '<span style="background:{}; color:white; padding:4px 10px; '
            'border-radius:12px; font-size:12px; font-weight:bold;">{}</span>',
            color, label
        )
    statut_badge.short_description = "Statut"

    def changer_statut(self, obj):
        if obj.status == 'PENDING':
            return format_html(
                '<a href="/admin/reports/report/{}/change/" '
                'style="background:#2980B9; color:white; padding:4px 8px; '
                'border-radius:6px; text-decoration:none; font-size:11px;">▶ Traiter</a>',
                obj.pk
            )
        elif obj.status == 'IN_PROGRESS':
            return format_html(
                '<a href="/admin/reports/report/{}/change/" '
                'style="background:#27AE60; color:white; padding:4px 8px; '
                'border-radius:6px; text-decoration:none; font-size:11px;">✅ Résoudre</a>',
                obj.pk
            )
        return format_html('<span style="color:#27AE60; font-size:12px;">✅ Terminé</span>')
    changer_statut.short_description = "Action"

    actions = ['marquer_en_cours', 'marquer_resolu', 'marquer_en_attente']

    def marquer_en_cours(self, request, queryset):
        queryset.update(status='IN_PROGRESS')
        self.message_user(request, f"{queryset.count()} signalement(s) marqué(s) En cours.")
    marquer_en_cours.short_description = "🔄 Marquer En cours"

    def marquer_resolu(self, request, queryset):
        queryset.update(status='RESOLVED')
        self.message_user(request, f"{queryset.count()} signalement(s) marqué(s) Résolu.")
    marquer_resolu.short_description = "✅ Marquer Résolu"

    def marquer_en_attente(self, request, queryset):
        queryset.update(status='PENDING')
        self.message_user(request, f"{queryset.count()} signalement(s) marqué(s) En attente.")
    marquer_en_attente.short_description = "⏳ Marquer En attente"


# ✅ Enregistrer dans les DEUX admins
admin.site.register(Report, ReportAdmin)
custom_admin.register(Report, ReportAdmin)