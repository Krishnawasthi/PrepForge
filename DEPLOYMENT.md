# 🚀 Deployment Guide: PrepForge on Vercel

PrepForge is composed of two clean components:
1. **Frontend**: Next.js 14 App Router (Deployed on **Vercel**)
2. **Backend**: Spring Boot 3 Java 21 REST API (Deployed on **Render**, **Railway**, or **Fly.io**)

---

## Step 1: Deploy Backend (Render / Railway / Koyeb)

Your Spring Boot backend handles AI test generation via the Gemini API and serves REST endpoints.

### Option A: Deploy on [Render.com](https://render.com) (Free / Instant)
1. Sign in to [Render.com](https://render.com) and click **New +** → **Web Service**.
2. Connect your GitHub repository: `Krishnawasthi/PrepForge`.
3. Configure the service:
   - **Name**: `prepforge-backend`
   - **Root Directory**: `prepforge-backend`
   - **Runtime**: `Docker` (Render will automatically detect `prepforge-backend/Dockerfile`)
   - **Instance Type**: Free
4. Add **Environment Variables**:
   - `GEMINI_API_KEY`: `your_gemini_api_key_here` (your Google AI Studio / Gemini API key)
   - `PORT`: `8080`
   - *(Optional)* `MONGODB_URI`: Your MongoDB Atlas URI if you want cloud MongoDB persistence (otherwise in-memory fallback is active).
5. Click **Create Web Service**.
6. Once deployed, copy your backend URL (e.g., `https://prepforge-backend.onrender.com`).

---

## Step 2: Deploy Frontend on [Vercel](https://vercel.com)

1. Go to [Vercel](https://vercel.com) and click **Add New...** → **Project**.
2. Import your GitHub repository: `Krishnawasthi/PrepForge`.
3. In the project setup screen:
   - **Framework Preset**: Next.js (automatically detected)
   - **Root Directory**: Click *Edit* and select **`prepforge-frontend`** *(Important!)*
4. Under **Environment Variables**, add:
   - `NEXT_PUBLIC_API_URL`: Your deployed backend URL (e.g. `https://prepforge-backend.onrender.com` without trailing slash)
   - `BACKEND_API_URL`: Your deployed backend URL (e.g. `https://prepforge-backend.onrender.com`)
5. Click **Deploy**.

Vercel will build and launch your Next.js frontend with an automatic SSL `https://*.vercel.app` domain.

---

## 🛠️ Verification Checklist

- [x] CORS in Spring Boot is pre-configured to accept requests from all `https://*.vercel.app` domains.
- [x] Backend port dynamically reads `${PORT}` on cloud environments.
- [x] Gemini API key is securely stored server-side in the backend environment variables with zero client-side exposure.
- [x] Next.js frontend automatically connects to the backend via `NEXT_PUBLIC_API_URL`.
