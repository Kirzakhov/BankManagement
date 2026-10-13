import { HttpErrorResponse } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, inject, OnInit, ViewChild } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { Router } from '@angular/router';
import { RouterLink } from '@angular/router';
import { finalize, timeout } from 'rxjs';
import { BankService, BankSessionService, BankTransaction, BankUser } from '../bank';

@Component({
  imports: [CommonModule, FormsModule, RouterLink],
  selector: 'app-admin-dashboard',
  styleUrl: './admin-dashboard.css',
  templateUrl: './admin-dashboard.html',
})
export class AdminDashboard implements OnInit {
  users: BankUser[] = [];
  transactions: BankTransaction[] = [];
  loading = true;
  saving = false;
  error = '';
  success = '';
  search = '';
  editingAccount: number | null = null;
  form: BankUser = this.emptyForm();
  @ViewChild('customerForm') private customerForm?: NgForm;

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

  get customers(): BankUser[] {
    const query = this.search.trim().toLowerCase();
    return this.users
      .filter((user) => user.username !== 'admin')
      .filter((user) => !query || [user.name, user.username, user.email, String(user.acno)]
        .some((value) => value?.toLowerCase().includes(query)));
  }

  transactionLabel(type: BankTransaction['trtype']): string {
    return type === 'TRANSFER_RECEIVED' ? 'TRANSFER RECEIVED' : type;
  }

  refresh(): void {
    const credentials = this.session;
    if (!credentials) return;
    this.loading = true;
    this.error = '';
    this.bank.getUsers(credentials.username, credentials.password).subscribe({
      next: (users) => {
        this.users = users;
        this.loading = false;
        this.changeDetector.markForCheck();
      },
      error: (error: HttpErrorResponse) => {
        this.loading = false;
        this.error = this.errorMessage(error, 'Could not load customer accounts.');
        this.changeDetector.markForCheck();
      },
    });
    this.bank.getAdminTransactions(8, credentials.username, credentials.password).subscribe({
      next: (transactions) => {
        this.transactions = transactions;
        this.changeDetector.markForCheck();
      },
      error: (error: HttpErrorResponse) => {
        if (error.status !== 404) this.error = this.errorMessage(error, 'Could not load recent transactions.');
        this.changeDetector.markForCheck();
      },
    });
  }

  beginCreate(): void {
    this.editingAccount = null;
    this.resetCreateForm();
    this.success = '';
    this.error = '';
  }

  edit(user: BankUser): void {
    this.editingAccount = user.acno ?? null;
    this.form = { ...user, password: '' };
    this.success = '';
    this.error = '';
  }

  cancelEdit(): void {
    this.editingAccount = null;
    this.resetCreateForm();
  }

  save(): void {
    const credentials = this.session;
    if (!credentials) return;
    this.error = '';
    this.success = '';
    this.saving = true;

    const request = this.editingAccount
      ? this.bank.updateUser(this.editingAccount, {
          name: this.form.name,
          cellno: this.form.cellno,
          email: this.form.email,
          address: this.form.address,
        }, credentials.username, credentials.password)
      : this.bank.saveUser({ ...this.form, balance: 10000 }, credentials.username, credentials.password);

    request.pipe(
      timeout({ first: 15000 }),
      finalize(() => {
        this.saving = false;
        this.changeDetector.markForCheck();
      }),
    ).subscribe({
      next: (user) => {
        if (user.acno) {
          const existingIndex = this.users.findIndex((item) => item.acno === user.acno);
          if (existingIndex >= 0) {
            this.users = this.users.map((item) => item.acno === user.acno ? user : item);
          } else {
            this.users = [...this.users, user];
          }
        }
        this.success = this.editingAccount ? 'Customer details updated.' : 'Customer account created.';
        this.cancelEdit();
        this.changeDetector.markForCheck();
      },
      error: (error: unknown) => {
        this.error = error instanceof Error && error.name === 'TimeoutError'
          ? 'The server did not confirm the save within 15 seconds. Check the customer list before retrying.'
          : error instanceof HttpErrorResponse
            ? this.errorMessage(error, 'Could not save the customer account. Check that unique fields are not already in use.')
            : 'Could not save the customer account.';
        this.changeDetector.markForCheck();
      },
    });
  }

  remove(user: BankUser): void {
    const credentials = this.session;
    if (!credentials || !user.acno || !window.confirm(`Delete the account for ${user.name} (#${user.acno})?`)) return;
    this.error = '';
    this.success = '';
    this.bank.deleteUser(user.acno, credentials.username, credentials.password).subscribe({
      next: () => {
        this.success = `Account #${user.acno} deleted.`;
        this.refresh();
        this.changeDetector.markForCheck();
      },
      error: (error: HttpErrorResponse) => {
        this.error = this.errorMessage(error, 'Could not delete the account.');
        this.changeDetector.markForCheck();
      },
    });
  }

  logout(): void {
    this.sessionService.end();
    void this.router.navigate(['/login']);
  }

  private emptyForm(): BankUser {
    return { name: '', cellno: '', email: '', address: '', pan: '', adhaar: '', username: '', password: '' };
  }

  private resetCreateForm(): void {
    this.form = this.emptyForm();
    this.customerForm?.resetForm(this.form);
  }

  private errorMessage(error: HttpErrorResponse, fallback: string): string {
    const message = error.error?.message;
    return typeof message === 'string' && message ? message : fallback;
  }
}
