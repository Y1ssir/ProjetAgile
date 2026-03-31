from django.contrib import admin
from django.urls import path, include
from django.conf import settings
from django.conf.urls.static import static
from django.contrib.auth import views as auth_views
from reports import views as report_views
from reports.admin import custom_admin  # ← ici en haut !

urlpatterns = [
    # Page d'accueil
    path('', report_views.home, name='home'),
    # Tableau de bord
    path('dashboard/', report_views.dashboard, name='dashboard'),
    # Auth
    path('login/', auth_views.LoginView.as_view(template_name='reports/login.html'), name='login'),
    path('logout/', auth_views.LogoutView.as_view(next_page='/'), name='logout'),
    path('register/', report_views.register, name='register'),
    # Admin Django
    path('admin/', custom_admin.urls),
    # Reports
    path('reports/', include('reports.urls')),
]

if settings.DEBUG:
    urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)