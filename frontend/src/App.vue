<template>
  <RouterView />
</template>

<script setup>
import { RouterView } from 'vue-router'
</script>

<style>
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background: #f8f9fa;
  color: #212529;
  line-height: 1.5;
}

a {
  text-decoration: none;
  color: inherit;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 16px;
  border-radius: 6px;
  border: none;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: opacity 0.2s, transform 0.1s;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn:not(:disabled):active {
  transform: scale(0.98);
}

.btn-primary {
  background: #4F46E5;
  color: white;
}

.btn-primary:not(:disabled):hover {
  background: #4338CA;
}

.btn-danger {
  background: #EF4444;
  color: white;
}

.btn-danger:not(:disabled):hover {
  background: #DC2626;
}

.btn-secondary {
  background: #6B7280;
  color: white;
}

.btn-secondary:not(:disabled):hover {
  background: #4B5563;
}

.btn-outline {
  background: transparent;
  color: #4F46E5;
  border: 1px solid #4F46E5;
}

.btn-outline:not(:disabled):hover {
  background: #EEF2FF;
}

.card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  padding: 24px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 16px;
}

.form-group label {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.form-group input,
.form-group select {
  padding: 8px 12px;
  border: 1px solid #D1D5DB;
  border-radius: 6px;
  font-size: 14px;
  color: #212529;
  transition: border-color 0.2s;
  outline: none;
}

.form-group input:focus,
.form-group select:focus {
  border-color: #4F46E5;
  box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.1);
}

.form-group input.error,
.form-group select.error {
  border-color: #EF4444;
}

.field-error {
  font-size: 12px;
  color: #EF4444;
}

.field-hint {
  font-size: 12px;
  color: #6B7280;
}

.field-hint.match {
  color: #10B981;
}

.form-group input:read-only {
  background: #F9FAFB;
  color: #6B7280;
  cursor: default;
}

.alert {
  padding: 12px 16px;
  border-radius: 6px;
  font-size: 14px;
  margin-bottom: 16px;
}

.alert-error {
  background: #FEF2F2;
  color: #991B1B;
  border: 1px solid #FECACA;
}

.alert-success {
  background: #F0FDF4;
  color: #166534;
  border: 1px solid #BBF7D0;
}

.badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  border-radius: 99px;
  font-size: 12px;
  font-weight: 500;
}

.badge-success {
  background: #D1FAE5;
  color: #065F46;
}

.badge-danger {
  background: #FEE2E2;
  color: #991B1B;
}

.badge-warning {
  background: #FEF3C7;
  color: #92400E;
}

.badge-gray {
  background: #F3F4F6;
  color: #4B5563;
}

table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

table th {
  text-align: left;
  padding: 12px 16px;
  background: #F9FAFB;
  color: #6B7280;
  font-weight: 600;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  border-bottom: 1px solid #E5E7EB;
}

table td {
  padding: 12px 16px;
  border-bottom: 1px solid #F3F4F6;
  color: #374151;
}

table tbody tr:hover {
  background: #F9FAFB;
}

.loading-spinner {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px;
  color: #6B7280;
  font-size: 14px;
  gap: 8px;
}

.spinner {
  width: 20px;
  height: 20px;
  border: 2px solid #E5E7EB;
  border-top-color: #4F46E5;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.page-header h1 {
  font-size: 20px;
  font-weight: 600;
  color: #111827;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: white;
  border-radius: 12px;
  padding: 32px;
  width: 100%;
  max-width: 420px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

.link-btn {
  background: none;
  border: none;
  color: #4F46E5;
  cursor: pointer;
  font-size: 14px;
  padding: 0;
  text-decoration: underline;
}

.link-btn:hover {
  color: #4338CA;
}
</style>
