import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-oauth-redirect',
  standalone: true,
  template: '<p style="text-align: center; margin-top: 50px; font-size: 18px;">Authenticating securely...</p>'
})
export class OauthRedirect implements OnInit {

  constructor(
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit() {
    // 1. Read the data Spring Boot put in the URL
    this.route.queryParams.subscribe(params => {
      const userId = params['userId'];
      const isApproved = params['approved'] === 'true'; // Convert string to boolean
      const isProfileComplete = params['profileComplete'] === 'true';

      if (userId) {
        // 2. Save the user ID in the browser's local storage so we stay logged in
        localStorage.setItem('userId', userId);
        
        // 3. Apply your IMS Noida Gatekeeper Rules!
       if (!isProfileComplete) {
          // If the profile isn't done, ALWAYS send them to the form so we can ask for their email!
          this.router.navigate(['/onboarding']);
        } 
        else if (!isApproved) {
          // If the profile IS done, but they still aren't approved, block them.
          alert('Access Denied: Your account is pending admin approval.');
          this.router.navigate(['/login']);
        } 
        else {
          // Approved and Profile Complete! Send them to the main portal!
          this.router.navigate(['/dashboard']); 
        }
      } else {
        // If the URL is missing the userId, something went wrong. Go back to login.
        this.router.navigate(['/login']);
      }
    });
  }
}