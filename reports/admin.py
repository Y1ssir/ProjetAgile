from django.contrib import admin
from .models import Report

# Register your models here.
@admin.register(Report)
class ReportAdmin(admin.ModelAdmin):
    list_display = ('title', 'category', 'author', 'created_at')
    list_filter = ('category', 'created_at') 
    search_fields = ('title', 'description')