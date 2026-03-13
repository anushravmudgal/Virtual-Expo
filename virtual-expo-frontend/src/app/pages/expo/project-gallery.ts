import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';


@Component({
  selector: 'app-project-gallery',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './project-gallery.html',
  styleUrls: ['./project-gallery.css']
})
export class ProjectGallery implements OnInit {
  allProjects: any[] = [];
  filteredProjects: any[] = [];
  
  // Filter states
  searchQuery: string = '';
  selectedCourse: string = 'All';

  constructor(private http: HttpClient,private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.loadProjects();
  }

loadProjects() {
    // 1. I added withCredentials back in temporarily just to test the connection
    this.http.get('http://localhost:8088/api/groups', { withCredentials: true })
      .subscribe({
        next: (data: any) => {
          console.log("RAW DATA FROM SPRING BOOT:", data); // 2. Print exactly what the database sees
          
          // 3. Temporarily show ALL groups, even if they don't have a GitHub URL yet
          this.allProjects = data; 
          this.filteredProjects = [...this.allProjects];
          this.cdr.detectChanges();
        },
        error: (err) => console.error('Error loading projects:', err)
      });
  }

  applyFilters() {
    this.filteredProjects = this.allProjects.filter(project => {
      const matchesSearch = project.projectTitle.toLowerCase().includes(this.searchQuery.toLowerCase()) || 
                            project.name.toLowerCase().includes(this.searchQuery.toLowerCase());
      
      // If we implement course filtering, we check the leader's course
      const matchesCourse = this.selectedCourse === 'All' || project.leader?.course === this.selectedCourse;
      
      return matchesSearch && matchesCourse;
    });
  }
}