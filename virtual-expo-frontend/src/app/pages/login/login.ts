import { Component } from '@angular/core';

@Component({
  selector: 'app-login',
  standalone: true,
  templateUrl: './login.html',
  styleUrls: ['./login.css']
})
export class Login {

  // This is the exact default URL Spring Security generates to start the OAuth flow
  loginWithGithub() {
    window.location.href = 'http://localhost:8088/oauth2/authorization/github';
  }
}