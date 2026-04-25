from django.contrib import admin
from django.contrib.admin import AdminSite
from django.utils.html import format_html
from django.urls import path
from django.shortcuts import render
from django.db.models import Count
from .models import Report
from django.utils.safestring import mark_safe


# ============================================================
# ADMIN SITE PERSONNALISÉ
# ============================================================
class CustomAdminSite(AdminSite):
    site_header = "🌱 Campus Éco-IA — Administration"
    site_title = "Campus Éco-IA Admin"
    index_title = "Acceuil"

    def get_urls(self):
        urls = super().get_urls()
        custom_urls = [
            path('dashboard/', self.admin_view(self.dashboard_view), name='dashboard'),
        ]
        return custom_urls + urls

    def dashboard_view(self, request):
        # ── Stats globales (avec resolution_rate) ──────────────────
        total       = Report.objects.count()
        resolved    = Report.objects.filter(status='RESOLVED').count()
        stats = {
            'total'           : total,
            'pending'         : Report.objects.filter(status='PENDING').count(),
            'in_progress'     : Report.objects.filter(status='IN_PROGRESS').count(),
            'resolved'        : resolved,
            'resolution_rate' : round((resolved / total * 100), 1) if total > 0 else 0,
        }

        # ── Catégories : labels lisibles ───────────────────────────
        category_dict = dict(Report.CATEGORY_CHOICES)
        category_stats = [
            {
                'label': category_dict.get(item['category'], item['category'])[:15],
                'count': item['count'],
            }
            for item in Report.objects.values('category').annotate(count=Count('id'))
        ]

        # ── Signalements récents ────────────────────────────────────
        recent_reports = Report.objects.select_related('author').order_by('-created_at')[:10]

        return render(request, 'admin/dashboard.html', {
            'stats'          : stats,
            'category_stats' : category_stats,   # ← était 'category_data' avant !
            'recent_reports' : recent_reports,
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
        return mark_safe('<span style="color:#27AE60; font-size:12px;">✅ Terminé</span>')
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