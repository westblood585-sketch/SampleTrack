import { Routes, Route } from "react-router-dom";
import { Layout } from "./components/Layout";
import Dashboard from "./pages/Dashboard";
import SampleList from "./pages/SampleList";
import SampleCreate from "./pages/SampleCreate";
import SampleDetail from "./pages/SampleDetail";
import TestDefinitions from "./pages/TestDefinitions";
import Customers from "./pages/Customers";

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<Dashboard />} />
        <Route path="samples" element={<SampleList />} />
        <Route path="samples/new" element={<SampleCreate />} />
        <Route path="samples/:id" element={<SampleDetail />} />
        <Route path="test-definitions" element={<TestDefinitions />} />
        <Route path="customers" element={<Customers />} />
      </Route>
    </Routes>
  );
}
