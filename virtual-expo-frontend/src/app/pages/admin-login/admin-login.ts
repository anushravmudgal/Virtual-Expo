import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

@Component({
  selector: 'app-admin-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-login.html',
  styleUrls: ['./admin-login.css']
})
export class AdminLogin {
  email = '';
  password = '';
  errorMessage = '';
  isLoading = false;

  constructor(private http: HttpClient, private router: Router) {}

  onAdminLogin() {
    if (!this.email || !this.password) {
      this.errorMessage = 'Please enter both email and password.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    const credentials = {
      email: this.email,
      password: this.password
    };

    this.http.post('http://localhost:8088/api/admin/login', credentials)
      .subscribe({
        next: (adminData: any) => {
          // Success! Save the admin token/ID and route to the command center
          localStorage.setItem('adminId', adminData.id);
          localStorage.setItem('adminName', adminData.username);
          
          // We will build this dashboard next!
          this.router.navigate(['/admin/dashboard']); 
        },
        error: (err) => {
          this.isLoading = false;
          if (err.status === 401 || err.status === 403) {
            this.errorMessage = 'Invalid faculty credentials or unauthorized access.';
          } else {
            this.errorMessage = 'System error. Please contact the System Developer.';
          }
        }
      });
  }
}