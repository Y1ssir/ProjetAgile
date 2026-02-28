from django.urls import path
from . import views

app_name = 'reports'

urlpatterns = [
    path('', views.report_list, name='report_list'),
    path('nouveau/', views.report_create, name='report_create'),
    path('merci/', views.report_success, name='report_success'),
    path('<int:pk>/', views.report_detail, name='report_detail'),
]