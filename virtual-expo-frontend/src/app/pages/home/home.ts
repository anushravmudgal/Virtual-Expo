import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule], // We need RouterModule for the navigation links!
  templateUrl: './home.html',
  styleUrls: ['./home.css']
})
export class Home {
  // Logic can be added here later if we want dynamic stats (e.g., "50+ Projects Live!")
}