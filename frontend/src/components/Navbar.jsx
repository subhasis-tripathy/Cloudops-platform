import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../AuthContext";

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <header className="navbar">
      <div className="navbar-inner">
        <span className="brand">CloudOps</span>
        {user ? (
          <>
            <nav className="nav-links">
              <NavLink to="/" end>Dashboard</NavLink>
              <NavLink to="/projects">Projects</NavLink>
            </nav>
            <div className="nav-user">
              <span className="muted">{user.email}</span>
              <span className="badge badge-role">{user.role}</span>
              <button className="btn btn-ghost" onClick={handleLogout}>Log out</button>
            </div>
          </>
        ) : (
          <nav className="nav-links">
            <NavLink to="/login">Log in</NavLink>
            <NavLink to="/register">Register</NavLink>
          </nav>
        )}
      </div>
    </header>
  );
}
