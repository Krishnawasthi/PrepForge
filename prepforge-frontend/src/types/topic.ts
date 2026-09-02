export interface SubTopic {
  id: string;
  name: string;
  description: string;
  estimatedQuestionCount: number;
}

export interface Topic {
  id: string;
  name: string;
  slug: string;
  category: string;
  description: string;
  icon: string;
  badgeColor: string;
  popular: boolean;
  subTopics: SubTopic[];
}
