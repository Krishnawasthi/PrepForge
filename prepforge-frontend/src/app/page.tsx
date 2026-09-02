import { HeroSection } from "@/components/home/HeroSection";
import { LiveSystemStatus } from "@/components/home/LiveSystemStatus";
import { TopicCloud } from "@/components/home/TopicCloud";
import { FeaturesSection } from "@/components/home/FeaturesSection";
import { TestBuilder } from "@/components/builder/TestBuilder";

export default function HomePage() {
  return (
    <div className="flex flex-col min-h-full space-y-12">
      <HeroSection />
      
      {/* Test Builder Section */}
      <section className="relative z-10 -mt-4">
        <TestBuilder />
      </section>

      {/* Live System Diagnostics */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 w-full">
        <LiveSystemStatus />
      </div>

      <FeaturesSection />
      <TopicCloud />
    </div>
  );
}
