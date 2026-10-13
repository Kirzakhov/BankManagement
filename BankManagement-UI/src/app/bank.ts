import { inject, Injectable, signal } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export type BankRole = 'ADMIN' | 'USER';

export interface BankSession {
  username: string;
  password: string;
  role: BankRole;
  accountNumber?: number;
}

export interface BankUser {
  acno?: number;
  name: string;
  cellno: string;
  email: string;
  address: string;
  pan?: string;
  adhaar?: string;
  balance?: number;
  username: string;
  password?: string;
}

export interface BankTransaction {
  id?: number;
  amount: number;
  trtype: 'DEPOSIT' | 'WITHDRAW' | 'TRANSFER' | 'TRANSFER_RECEIVED';
  racno?: number;
  time?: string;
  user?: { acno: number; name: string };
}

@Injectable({ providedIn: 'root' })
export class BankSessionService {
  private readonly current = signal<BankSession | null>(null);
  readonly session = this.current.asReadonly();

  start(session: BankSession): void {
    this.current.set(session);
  }

  end(): void {
    this.current.set(null);
  }
}

@Injectable({ providedIn: 'root' })
export class BankService {
  private readonly base = '/api';
  private readonly http = inject(HttpClient);

  private authHeaders(username: string, password: string): HttpHeaders {
    return new HttpHeaders({
      Authorization: `Basic ${btoa(`${username}:${password}`)}`,
    });
  }

  getUsers(username: string, password: string): Observable<BankUser[]> {
    return this.http.get<BankUser[]>(`${this.base}/admin/`, {
      headers: this.authHeaders(username, password),
    });
  }

  getUser(acno: number, username: string, password: string): Observable<BankUser> {
    return this.http.get<BankUser>(`${this.base}/admin/${acno}`, {
      headers: this.authHeaders(username, password),
    });
  }

  saveUser(user: BankUser, username: string, password: string): Observable<BankUser> {
    return this.http.post<BankUser>(`${this.base}/admin/save/`, user, {
      headers: this.authHeaders(username, password),
    });
  }

  updateUser(acno: number, user: Partial<BankUser>, username: string, password: string): Observable<BankUser> {
    return this.http.put<BankUser>(`${this.base}/admin/update/${acno}`, user, {
      headers: this.authHeaders(username, password),
    });
  }

  deleteUser(acno: number, username: string, password: string): Observable<string> {
    return this.http.delete(`${this.base}/admin/delete/${acno}`, {
      headers: this.authHeaders(username, password),
      responseType: 'text',
    });
  }

  getAdminTransactions(n: number, username: string, password: string): Observable<BankTransaction[]> {
    return this.http.get<BankTransaction[]>(`${this.base}/admin/getTrans/${n}`, {
      headers: this.authHeaders(username, password),
    });
  }

  getUserDetails(acno: number, username: string, password: string): Observable<BankUser> {
    return this.http.get<BankUser>(`${this.base}/users/get/${acno}`, {
      headers: this.authHeaders(username, password),
    });
  }

  updateUserDetails(acno: number, user: Partial<BankUser>, username: string, password: string): Observable<BankUser> {
    return this.http.put<BankUser>(`${this.base}/users/update/${acno}`, user, {
      headers: this.authHeaders(username, password),
    });
  }

  getBalance(acno: number, username: string, password: string): Observable<string> {
    return this.http.get(`${this.base}/users/balance/${acno}`, {
      headers: this.authHeaders(username, password),
      responseType: 'text',
    });
  }

  doTransaction(acno: number, transaction: BankTransaction, username: string, password: string): Observable<string> {
    return this.http.put(`${this.base}/users/${acno}`, transaction, {
      headers: this.authHeaders(username, password),
      responseType: 'text',
    });
  }

  getUserTransactions(acno: number, n: number, username: string, password: string): Observable<BankTransaction[]> {
    return this.http.get<BankTransaction[]>(`${this.base}/users/${acno}/${n}`, {
      headers: this.authHeaders(username, password),
    });
  }
}
