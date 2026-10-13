import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { BankService, BankSessionService } from '../bank';

@Component({
  imports: [FormsModule],
  selector: 'app-login',
  styleUrl: './login.css',
  templateUrl: './login.html',
})
export class Login {
  username = '';
  password = '';
  accountNumber: number | null = null;
  error = '';
  loading = false;

  private readonly bank = inject(BankService);
  private readonly session = inject(BankSessionService);
  private readonly router = inject(Router);
  private readonly changeDetector = inject(ChangeDetectorRef);

  login(): void {
    this.error = '';
    const username = this.username.trim();
    const isAdmin = username === 'admin';
    if (!username || !this.password || (!isAdmin && (!this.accountNumber || this.accountNumber <= 0))) {
      this.error = isAdmin
        ? 'Enter your username and password.'
        : 'Enter your username, password, and account number.';
      return;
    }

    this.loading = true;
    const verified: Observable<unknown> = isAdmin
      ? this.bank.getUsers(username, this.password)
      : this.bank.getUserDetails(this.accountNumber!, username, this.password);

    verified.subscribe({
      next: () => {
        this.session.start({
          username,
          password: this.password,
          role: isAdmin ? 'ADMIN' : 'USER',
          accountNumber: isAdmin ? undefined : this.accountNumber!,
        });
        this.loading = false;
        this.changeDetector.markForCheck();
        void this.router.navigate([isAdmin ? '/admin' : '/account']);
      },
      error: (error: HttpErrorResponse) => {
        this.loading = false;
        this.error = error.status === 0
          ? 'Cannot connect to the bank API. Make sure the backend is running on port 8082.'
          : error.status === 401 || error.status === 403
            ? 'Login failed. Check your credentials and account role.'
            : this.errorMessage(error, 'Could not sign in.');
        this.changeDetector.markForCheck();
      },
    });
  }

  private errorMessage(error: HttpErrorResponse, fallback: string): string {
    const message = error.error?.message;
    return typeof message === 'string' && message ? message : fallback;
  }
}
