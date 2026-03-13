import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-onboarding',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './onboarding.html',
  styleUrls: ['./onboarding.css']
})
export class Onboarding implements OnInit {
  userId: string | null = '';
  
formData = {
    email: '',
    rollNumber: '',
    course: 'BCA',
    semester: 1,
    section: 'A',
    skills: '',
    preferredLanguage: '',
    targetItDomain: '',
    linkedinUrl: '',
    // Add the new fields here:
    dreamJobProfile: '',
    industry: '',
    targetCompanies: ''
  };

  constructor(private http: HttpClient, private router: Router) {}

  ngOnInit() {
    // Grab the ID we saved during the redirect step
    this.userId = localStorage.getItem('userId');
    if (!this.userId) {
      this.router.navigate(['/login']);
    }
  }

  submitProfile() {
    this.http.put(`http://localhost:8088/api/users/${this.userId}/profile`, this.formData, { withCredentials: true })
      .subscribe({
        next: (response: any) => {
          if (response.approved) {
            alert('Profile complete! Welcome to the IMS Noida Virtual Expo.');
            this.router.navigate(['/dashboard']); // We will build this next!
          } else {
            alert('Profile submitted! Your account is pending admin approval.');
            this.router.navigate(['/login']);
          }
        },
        error: (err) => {
          alert('Error saving profile. Please check your details.');
          console.error(err);
        }
      });
  }
}