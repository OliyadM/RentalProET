// Auth context will go here
import { createContext, useContext, useState } from "react";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(
    JSON.parse(localStorage.getItem("rentalpro_user")) || null
  );

  const login = (userData) => {
    setUser(userData);
    localStorage.setItem("rentalpro_user", JSON.stringify(userData));
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem("rentalpro_user");
  };

  // Call this after profile verification to sync accountStatus without re-login
  const refreshAccountStatus = (newStatus) => {
    if (!user) return;
    const updated = { ...user, accountStatus: newStatus };
    setUser(updated);
    localStorage.setItem("rentalpro_user", JSON.stringify(updated));
  };

  return (
    <AuthContext.Provider value={{ user, login, logout, refreshAccountStatus }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}