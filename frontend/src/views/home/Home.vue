<template>
  <div class="home">
    <div class="home__search">
      <div class="home__search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索书名、作者、出版社..."
          size="large"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button @click="handleSearch">搜索</el-button>
          </template>
        </el-input>
        <el-button size="large" @click="showAdvanced = !showAdvanced">
          {{ showAdvanced ? '收起' : '高级搜索' }}
        </el-button>
      </div>

      <el-collapse-transition>
        <div v-show="showAdvanced" class="home__advanced">
          <el-form :inline="true">
            <el-form-item label="位置">
              <el-input v-model="searchForm.locate" placeholder="交易地点" clearable />
            </el-form-item>
            <el-form-item label="价格区间">
              <el-input v-model="searchForm.minPrice" placeholder="最低" type="number" style="width: 100px" />
              <span style="margin: 0 6px">-</span>
              <el-input v-model="searchForm.maxPrice" placeholder="最高" type="number" style="width: 100px" />
            </el-form-item>
            <el-form-item label="排序">
              <el-select v-model="searchForm.sortDirection" placeholder="不限" clearable>
                <el-option label="价格升序" value="ASC" />
                <el-option label="价格降序" value="DESC" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleAdvancedSearch">搜索</el-button>
              <el-button @click="resetSearch">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-collapse-transition>
    </div>

    <!-- <div class="home__section">
      <h2 class="home__section-title">推荐书籍</h2>
      <div class="home__book-grid">
        <BookCard
          v-for="book in recommendBooks"
          :key="book.id"
          v-bind="book"
        />
      </div>
      <EmptyState v-if="recommendBooks.length === 0 && !loading" text="暂无推荐书籍" />
    </div> -->

    <div class="home__section">
      <div class="home__section-header">
        <h2 class="home__section-title">{{ searching ? '搜索结果' : '全部书籍' }}</h2>
        <el-button v-if="searching" text type="primary" @click="resetSearch">清除搜索</el-button>
      </div>
      <div class="home__book-grid">
        <BookCard
          v-for="book in books"
          :key="book.id"
          v-bind="book"
        />
      </div>
      <EmptyState v-if="books.length === 0 && !loading" text="暂无书籍" />
      <Pagination
        v-model:currentPage="page"
        :total="totalBooks"
        :pageSize="pageSize"
        @update:currentPage="fetchBooks"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getBookList, searchBooks, advancedSearch } from '@/api/book'
// import { getRecommend } from '@/api/book'
import BookCard from '@/components/BookCard.vue'
import EmptyState from '@/components/EmptyState.vue'
import Pagination from '@/components/Pagination.vue'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const searchKeyword = ref('')
const showAdvanced = ref(false)
const searching = ref(false)
const page = ref(1)
const pageSize = 12
const totalBooks = ref(0)

const searchForm = reactive({
  locate: '',
  minPrice: '',
  maxPrice: '',
  sortDirection: '',
})

const books = ref([])
// const recommendBooks = ref([])

onMounted(async () => {
  await fetchBooks()
  // await fetchRecommend()
})

async function fetchBooks() {
  loading.value = true
  try {
    if (searching.value) {
      if (searchKeyword.value && Object.values(searchForm).some(v => v)) {
        const data = await advancedSearch({
          keyword: searchKeyword.value,
          ...searchForm,
        })
        books.value = Array.isArray(data) ? data : []
        totalBooks.value = books.value.length
      } else if (searchKeyword.value) {
        const data = await searchBooks(searchKeyword.value)
        books.value = Array.isArray(data) ? data : []
        totalBooks.value = books.value.length
      } else {
        const data = await advancedSearch(searchForm)
        books.value = Array.isArray(data) ? data : []
        totalBooks.value = books.value.length
      }
    } else {
      const data = await getBookList()
      books.value = Array.isArray(data) ? data : []
      totalBooks.value = books.value.length
    }
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

// async function fetchRecommend() {
//   try {
//     const data = await getRecommend(5)
//     recommendBooks.value = Array.isArray(data) ? data : []
//   } catch {
//     // silent
//   }
// }

function handleSearch() {
  searching.value = true
  page.value = 1
  fetchBooks()
}

function handleAdvancedSearch() {
  searching.value = true
  page.value = 1
  fetchBooks()
}

function resetSearch() {
  searchKeyword.value = ''
  searchForm.locate = ''
  searchForm.minPrice = ''
  searchForm.maxPrice = ''
  searchForm.sortDirection = ''
  searching.value = false
  showAdvanced.value = false
  page.value = 1
  fetchBooks()
}
</script>

<style lang="scss" scoped>
@use '@/styles/variables' as *;

.home {
  &__search {
    background: $bg-white;
    border-radius: 6px;
    padding: 20px;
    margin-bottom: 20px;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  }

  &__search-bar {
    display: flex;
    gap: 12px;

    .el-input {
      flex: 1;
    }
  }

  &__advanced {
    margin-top: 16px;
    padding-top: 16px;
    border-top: 1px solid $border-color;
  }

  &__section {
    margin-bottom: 28px;
  }

  &__section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  &__section-title {
    font-size: 18px;
    font-weight: 600;
    color: $text-primary;
    margin-bottom: 16px;
  }

  &__book-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;

    @media (max-width: 900px) {
      grid-template-columns: repeat(3, 1fr);
    }

    @media (max-width: 600px) {
      grid-template-columns: repeat(2, 1fr);
    }
  }
}
</style>
