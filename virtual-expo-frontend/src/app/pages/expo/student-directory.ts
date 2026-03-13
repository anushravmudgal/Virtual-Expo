import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-student-directory',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './student-directory.html',
  styleUrls: ['./student-directory.css']
})
export class StudentDirectory implements OnInit {
  allStudents: any[] = [];
  filteredStudents: any[] = [];
  searchQuery: string = '';

 
  constructor(private http: HttpClient,private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.loadStudents();
  }

  loadStudents() {
    // We don't need credentials because we added this to the public VIP list!
    this.http.get('http://localhost:8088/api/users')
      .subscribe({
        next: (data: any) => {
          this.allStudents = data;
          this.filteredStudents = [...this.allStudents];
                this.cdr.detectChanges();
        },
        error: (err) => console.error('Error loading students', err)
      });
  }

  applyFilters() {
    this.filteredStudents = this.allStudents.filter(student => {
      const searchStr = `${student.username} ${student.skills} ${student.targetItDomain}`.toLowerCase();
      return searchStr.includes(this.searchQuery.toLowerCase());
    });
  }
}