import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.css']
})
export class Dashboard implements OnInit {
  userId: string | null = '';
  availableGroups: any[] = [];
  
  // State Variables
  myGroup: any = null; 
  
  // Project Submission Fields
  projectTitle: string = '';
  githubUrl: string = '';
  liveUrl: string = ''; 
  projectDescription: string = ''; // <-- Ready for the PaaS update
  newGroupName: string = '';
  newGroupRole: string = 'Full Stack Developer';

  activeTab: string = 'workspace'; 
  userProfile: any = {}; 

  constructor(private http: HttpClient, private router: Router, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.userId = localStorage.getItem('userId');
    if (!this.userId) {
      this.router.navigate(['/login']);
      return;
    }
    this.loadUserProfile(); 
    this.checkMyGroup();
  }

  loadUserProfile() {
    this.http.get(`http://localhost:8088/api/users/${this.userId}`, { withCredentials: true })
      .subscribe({
        next: (data) => {
          this.userProfile = data;
          this.cdr.detectChanges();
        },
        error: (err) => console.error('Error loading profile', err)
      });
  }

  switchTab(tabName: string) {
    this.activeTab = tabName;
  }

  saveProfile() {
    this.http.put(`http://localhost:8088/api/users/${this.userId}/portfolio`, this.userProfile, { withCredentials: true })
      .subscribe({
        next: () => alert('Profile updated successfully! Recruiters will see these changes.'),
        error: () => alert('Error updating profile.')
      });
  }

  selectedFile: File | null = null;

  onFileSelected(event: any) {
    this.selectedFile = event.target.files[0];
  }

  uploadResume() {
    if (!this.selectedFile) return;

    const formData = new FormData();
    formData.append('file', this.selectedFile);

    this.http.post(`http://localhost:8088/api/users/${this.userId}/resume`, formData, { withCredentials: true })
      .subscribe({
        next: (response: any) => {
          alert('Resume uploaded directly to the IMS server!');
          this.userProfile.resumeUrl = response.resumeUrl; 
          this.selectedFile = null; 
          this.cdr.detectChanges();
        },
        error: (err) => alert('Error uploading resume.')
      });
  }

  checkMyGroup() {
    this.http.get(`http://localhost:8088/api/users/${this.userId}/group`, { withCredentials: true })
      .subscribe({
        next: (data: any) => {
          if (data) {
            this.myGroup = data;
            // Pre-fill existing data
            this.projectTitle = data.projectTitle || '';
            this.githubUrl = data.githubUrl || '';
            this.liveUrl = data.liveUrl || '';
            this.projectDescription = data.projectDescription || ''; // <-- Added to pre-fill
            this.cdr.detectChanges();
          } else {
            this.loadGroups(); 
          }
        },
        error: () => this.loadGroups() 
      });
  }

  loadGroups() {
    this.http.get('http://localhost:8088/api/groups', { withCredentials: true })
      .subscribe({
        next: (data: any) =>{
          this.availableGroups = data;
          this.cdr.detectChanges(); 
        },
        error: (err) => console.error('Error loading groups', err)
      });
  }

  createGroup() {
    if (!this.newGroupName.trim()) return;
    const url = `http://localhost:8088/api/groups/create/${this.userId}?groupName=${encodeURIComponent(this.newGroupName)}&role=${encodeURIComponent(this.newGroupRole)}`;
    
    this.http.post(url, {}, { withCredentials: true }).subscribe({
      next: () => {
        alert('Group Created Successfully!');
        this.checkMyGroup(); 
      },
      error: (err) => alert(err.error || 'Error creating group')
    });
  }

  joinGroup(groupId: number) {
    const role = prompt('What is your role in this team?', 'Frontend Developer');
    if (!role) return; 

    const url = `http://localhost:8088/api/groups/${groupId}/join/${this.userId}?role=${encodeURIComponent(role)}`;
    
    this.http.post(url, {}, { withCredentials: true }).subscribe({
      next: () => {
        alert('Successfully joined the team!');
        this.checkMyGroup(); 
      },
      error: (err) => alert(err.error || 'Error joining the team.')
    });
  }

  // UPDATED: Save project details to MySQL
  submitProject() {
    if (!this.myGroup) return;

    const projectData = {
      projectTitle: this.projectTitle,
      githubUrl: this.githubUrl,
      projectDescription: this.projectDescription // <-- Added to payload
    };

    this.http.put(`http://localhost:8088/api/groups/${this.myGroup.id}/project`, projectData, { withCredentials: true })
      .subscribe({
        next: (updatedGroup: any) => {
          alert('Project details submitted successfully!');
          this.myGroup = updatedGroup;
          this.cdr.detectChanges();
        },
        error: (err) => alert('Error saving project details.')
      });
  }

  // NEW: PaaS Deployment Trigger
triggerDeployment() {
  if (!this.myGroup || !this.githubUrl) {
    alert('Please save your GitHub URL first!');
    return;
  }

  // UPDATED URL: Added 's' to deployment and '/trigger' at the end
  this.http.post(`http://localhost:8088/api/deployments/${this.myGroup.id}/trigger`, {}, { withCredentials: true })
    .subscribe({
      next: (response: any) => {
        // Use response.id or response.deploymentId based on your Controller's Map.of()
        this.pollDeploymentStatus(response.id || response.deploymentId);
      },
      error: (err) => {
        console.error(err);
        alert('Failed to queue deployment. Check if the backend is running.');
      }
    });
}

pollDeploymentStatus(id: string) {
  const interval = setInterval(() => {
    // UPDATED URL: Match the @GetMapping("/status/{id}") in your Controller
    this.http.get(`http://localhost:8088/api/deployments/status/${id}`, { withCredentials: true })
      .subscribe({
        next: (res: any) => {
          if (res.status === 'RUNNING') {
            alert('🚀 Project is LIVE on IMS Cloud!');
            this.myGroup.liveUrl = res.url;
            clearInterval(interval);
            this.cdr.detectChanges();
          } else if (res.status === 'FAILED') {
            alert('❌ Build Failed: ' + res.error);
            clearInterval(interval);
          }
        },
        error: (err) => {
          console.error('Polling error:', err);
          // Don't clear interval on a single network glitch, but maybe after 5 fails
        }
      });
  }, 3000); 
}
}