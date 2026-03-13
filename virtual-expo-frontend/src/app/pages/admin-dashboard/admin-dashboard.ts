import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-dashboard.html',
  styleUrls: ['./admin-dashboard.css']
})
export class AdminDashboard implements OnInit {
  adminName: string | null = '';
  activeTab: string = 'students';
  
  allStudents: any[] = [];
  allProjects: any[] = [];

  constructor(private http: HttpClient, private router: Router) {}

  ngOnInit() {
    this.adminName = localStorage.getItem('adminName');
    if (!this.adminName) {
      this.router.navigate(['/imsadmin']);
      return;
    }
    this.loadData();
  }

  loadData() {
    this.http.get('http://localhost:8088/api/users').subscribe((data: any) => this.allStudents = data);
    this.http.get('http://localhost:8088/api/groups').subscribe((data: any) => this.allProjects = data);
  }

  switchTab(tab: string) {
    this.activeTab = tab;
  }

  deleteStudent(id: string) {
    if (confirm('Are you sure you want to remove this student from the system?')) {
      this.http.delete(`http://localhost:8088/api/users/${id}`).subscribe(() => this.loadData());
    }
  }

  deleteProject(id: string) {
    if (confirm('Are you sure you want to delete this project workspace?')) {
      this.http.delete(`http://localhost:8088/api/groups/${id}`).subscribe(() => this.loadData());
    }
  }

  logout() {
    localStorage.clear();
    this.router.navigate(['/']);
  }
}