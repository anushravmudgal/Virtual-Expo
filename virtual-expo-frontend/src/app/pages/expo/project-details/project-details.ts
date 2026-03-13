import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, RouterModule } from '@angular/router';

@Component({
  selector: 'app-project-details',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './project-details.html',
  styleUrls: ['./project-details.css']
})
export class ProjectDetails implements OnInit {
  project: any = null;
  isLoading = true;

  constructor(
    private http: HttpClient, 
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef // Grabs the ID from the URL
  ) {}

  ngOnInit() {
    // Get the ID from the URL bar
    const projectId = this.route.snapshot.paramMap.get('id');
    if (projectId) {
      this.loadProject(projectId);
    }
  }

  loadProject(id: string) {
    this.http.get(`http://localhost:8088/api/groups/${id}`)
      .subscribe({
        next: (data: any) => {
          this.project = data;
          this.isLoading = false;
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Error loading project', err);
          this.isLoading = false;
          this.cdr.detectChanges();
        }
      });
  }
}