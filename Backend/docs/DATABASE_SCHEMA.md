# RentalPro Database Schema Documentation

## Overview
This document describes the complete database schema for the RentalPro system.

**Database:** PostgreSQL 14
**Schema:** public
**Total Tables:** 10

---

## Table of Contents
1. [users](#1-users)
2. [properties](#2-properties)
3. [rental_units](#3-rental_units)
4. [rental_contracts](#4-rental_contracts)
5. [rent_declarations](#5-rent_declarations)
6. [appeals](#6-appeals)
7. [notifications](#7-notifications)
8. [audit_logs](#8-audit_logs)
9. [market_data](#9-market_data)
10. [system_config](#10-system_config)

---

## 1. users

**Purpose:** Stores all user accounts (Landlords, Tenants, Officers, Admins)

### Columns

| Column Name | Type | Constraints | Description |
|------------|------|-------------|-------------|
| id | UUID | PRIMARY KEY | Unique user identifier |
| first_name | VARCHAR(255) | NOT NULL | User's first name |
| last_name | VARC