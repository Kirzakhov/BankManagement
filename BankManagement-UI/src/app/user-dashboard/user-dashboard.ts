import { HttpErrorResponse } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, inject, OnInit, ViewChild } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { Router } from '@angular/router';
import { RouterLink } from '@angular/router';
import { BankService, BankSessionService, BankTransaction, BankUser } from '../bank';

@Component({
  imports: [CommonModule, FormsModule, RouterLink],
  selector: 'app-user-dashboard',
  styleUrl: './user-dashboard.css',
  templateUrl: './user-dashboard.html',
})
export class UserDashboard implements OnInit {
  account: BankUser | null = null;
  transactions: BankTransaction[] = [];
  balance = '';
  loading = true;
  savingProfile = false;
  submittingTransaction = false;
  error = '';
  success = '';
  recipientError = '';
  profile = { name: '', cellno: '', email: '', address: '', username: '', newPassword: '' };
  transaction: BankTransaction = { amount: 0, trtype: 'DEPOSIT', racno: undefined };
  @ViewChild('transactionForm') private transactionForm?: NgForm;

  private readonly bank = inject(BankService);
  private readonly sessionService = inject(BankSessionService);
  private readonly router = inject(Router);
  private readonly changeDetector = inject(ChangeDetectorRef);

  ngOnInit(): void {
    this.refresh();
  }

  get session() {
    return this.sessionService.session();
  }

  refresh(): void {
    const credentials = this.session;
    const acno = credentials?.accountNumber;
    if (!credentials || !acno) return;
    this.loading = true;
    this.error = '';

    this.bank.getUserDetails(acno, credentials.username, credentials.password).subscribe({
      next: (user) => {
        this.account = user;
        this.profile = {
          name: user.name,
          cellno: user.cellno,
          email: user.email,
          address: user.address,
          username: user.username,
          newPassword: '',
        };
        this.loading = false;
        this.changeDetector.markForCheck();
      },
      error: (error: HttpErrorResponse) => {
        this.loading = false;
        this.error = this.errorMessage(error, 'Could not load your account.');
        this.changeDetector.markForCheck();
      },
    });

    this.bank.getBalance(acno, credentials.username, credentials.password).subscribe({
      next: (balance) => {
        this.balance = balance;
        this.changeDetector.markForCheck();
      },
      error: (error: HttpErrorResponse) => {
        this.error = this.errorMessage(error, 'Could not load your balance.');
        this.changeDetector.markForCheck();
      },
    });
    this.bank.getUserTransactions(acno, 10, credentials.username, credentials.password).subscribe({
      next: (transactions) => {
        this.transactions = transactions;
        this.changeDetector.markForCheck();
      },
      error: (error: HttpErrorResponse) => {
        if (error.status === 404) this.transactions = [];
        else this.error = this.errorMessage(error, 'Could not load your transactions.');
        this.changeDetector.markForCheck();
      },
    });
  }

  saveProfile(): void {
    const credentials = this.session;
    const acno = credentials?.accountNumber;
    if (!credentials || !acno) return;
    this.error = '';
    this.success = '';
    this.savingProfile = true;
    const updatedCredentials = {
      username: this.profile.username.trim(),
      password: this.profile.newPassword || credentials.password,
    };
    this.bank.updateUserDetails(acno, {
      name: this.profile.name,
      cellno: this.profile.cellno,
      email: this.profile.email,
      address: this.profile.address,
      username: updatedCredentials.username,
      password: this.profile.newPassword || undefined,
    }, credentials.username, credentials.password).subscribe({
      next: () => {
        this.sessionService.start({ ...credentials, ...updatedCredentials });
        this.savingProfile = false;
        this.profile.newPassword = '';
        this.success = 'Your profile has been updated.';
        this.refresh();
        this.changeDetector.markForCheck();
      },
      error: (error: HttpErrorResponse) => {
        this.savingProfile = false;
        this.error = this.errorMessage(error, 'Could not update your profile. Check whether the username is already taken.');
        this.changeDetector.markForCheck();
      },
    });
  }

  submitTransaction(): void {
    const credentials = this.session;
    const acno = credentials?.accountNumber;
    if (!credentials || !acno) return;
    this.error = '';
    this.success = '';
    this.recipientError = '';
    if (!Number.isFinite(this.transaction.amount) || this.transaction.amount <= 0) {
      this.error = 'Enter an amount greater than zero.';
      return;
    }
    if (this.transaction.trtype === 'TRANSFER' && (!this.transaction.racno || this.transaction.racno <= 0)) {
      this.recipientError = 'Enter a valid recipient account number.';
      return;
    }
    if (this.transaction.trtype === 'TRANSFER' && this.transaction.racno === acno) {
      this.recipientError = 'You cannot transfer funds to your own account.';
      return;
    }

    this.recipientError = '';
    this.submittingTransaction = true;
    const payload: BankTransaction = {
      amount: Number(this.transaction.amount),
      trtype: this.transaction.trtype,
      ...(this.transaction.trtype === 'TRANSFER' ? { racno: Number(this.transaction.racno) } : {}),
    };
    this.bank.doTransaction(acno, payload, credentials.username, credentials.password).subscribe({
      next: (message) => {
        this.submittingTransaction = false;
        this.success = message || 'Transaction completed.';
        this.transaction = { amount: 0, trtype: 'DEPOSIT', racno: undefined };
        this.recipientError = '';
        this.transactionForm?.resetForm(this.transaction);
        this.refresh();
        this.changeDetector.markForCheck();
      },
      error: (error: HttpErrorResponse) => {
        this.submittingTransaction = false;
        if (payload.trtype === 'TRANSFER' && error.status === 404) {
          this.recipientError = 'That recipient account was not found. Check the account number and try again.';
        } else {
          this.error = this.errorMessage(error, 'The transaction could not be completed.');
        }
        this.changeDetector.markForCheck();
      },
    });
  }

  clearRecipientError(): void {
    this.recipientError = '';
  }

  transactionLabel(type: BankTransaction['trtype']): string {
    return type === 'TRANSFER_RECEIVED' ? 'TRANSFER RECEIVED' : type;
  }

  logout(): void {
    this.sessionService.end();
    void this.router.navigate(['/login']);
  }

  private errorMessage(error: HttpErrorResponse, fallback: string): string {
    const message = error.error?.message;
    return typeof message === 'string' && message ? message : fallback;
  }
}
